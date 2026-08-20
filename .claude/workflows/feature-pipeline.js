export const meta = {
  name: 'feature-pipeline',
  description: 'Plan, implement, and review a feature from a finalized story, retrying plan->implement->review up to 4 cycles',
  phases: [
    { title: 'Plan' },
    { title: 'Implement', model: 'haiku' },
    { title: 'Review' },
  ],
}

const MAX_CYCLES = 4

const PLAN_SCHEMA = {
  type: 'object',
  properties: {
    plan: { type: 'string' },
    choices: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          issue: { type: 'string' },
          decision: { type: 'string' },
          rationale: { type: 'string' },
        },
        required: ['issue', 'decision'],
      },
    },
  },
  required: ['plan', 'choices'],
}

const REVIEW_SCHEMA = {
  type: 'object',
  properties: {
    approved: { type: 'boolean' },
    issues: { type: 'array', items: { type: 'string' } },
  },
  required: ['approved', 'issues'],
}

const story = args.story

let plan = null
let issues = []
const allChoices = []
let approved = false
let cycle = 0

while (cycle < MAX_CYCLES && !approved) {
  cycle++

  phase('Plan')
  const plannerPrompt = plan
    ? `This is retry ${cycle} of ${MAX_CYCLES}.\n\nPrevious plan:\n${plan}\n\nA reviewer found these issues in an implementation of that plan:\n${issues.map(i => `- ${i}`).join('\n')}\n\nProduce a full, revised, self-contained plan that addresses every issue above. Write it as a complete plan from scratch — the implementer will not see this feedback or the previous plan, only the plan you write now.`
    : `Feature story:\n\n${story}\n\nProduce a full implementation plan for this story.`
  const planResult = await agent(plannerPrompt, {
    agentType: 'feature-planner',
    schema: PLAN_SCHEMA,
    phase: 'Plan',
    label: `plan-cycle-${cycle}`,
    effort: 'high',
  })
  plan = planResult.plan
  allChoices.push(...planResult.choices.map(c => ({ ...c, cycle })))
  log(`Cycle ${cycle}: plan ready (${planResult.choices.length} judgment call(s) made)`)

  phase('Implement')
  await agent(`Implement exactly this plan:\n\n${plan}`, {
    agentType: 'feature-implementer',
    phase: 'Implement',
    label: `implement-cycle-${cycle}`,
    model: 'haiku',
    effort: 'low',
  })
  log(`Cycle ${cycle}: implementation done`)

  phase('Review')
  const reviewResult = await agent(
    `Run \`git diff\` yourself in the current working tree to see the implementation changes made so far, and review them.\n\nFeature story, for context only (you have no plan to check against — review the diff on its own merits and against the existing codebase):\n\n${story}`,
    {
      agentType: 'feature-reviewer',
      schema: REVIEW_SCHEMA,
      phase: 'Review',
      label: `review-cycle-${cycle}`,
      effort: 'high',
    }
  )
  approved = reviewResult.approved
  issues = reviewResult.issues
  log(`Cycle ${cycle}: review ${approved ? 'approved' : `not approved (${issues.length} issue(s))`}`)
}

return {
  approved,
  cycles: cycle,
  finalPlan: plan,
  outstandingIssues: approved ? [] : issues,
  choices: allChoices,
}
