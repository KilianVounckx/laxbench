---
name: feature
description: Build a feature end-to-end through an isolated agent pipeline — interactive story capture, then a fully automated, non-interactive plan/implement/review loop (max 4 cycles) run via the feature-pipeline workflow. Use when the user asks to build, add, or implement a feature via this pipeline (e.g. "/feature", "build this feature", "run the feature pipeline for X").
---

This skill runs a feature from request to reviewed diff through four isolated stages: story capture (interactive, done by you directly), then plan, implement, review (all automated, no interaction, up to 4 retry cycles), orchestrated by the `feature-pipeline` workflow in `.claude/workflows/feature-pipeline.js` using the subagents defined in `.claude/agents/feature-planner.md`, `feature-implementer.md`, and `feature-reviewer.md`.

**Isolation is the point of this design**: the planner, implementer, and reviewer subagents never see each other's reasoning or communicate directly. They only ever receive exactly what the workflow script passes them — the story, the plan, or the diff (via their own `git diff`) — never anything else. Do not weaken this by, e.g., pasting the plan into the reviewer's prompt, or the story into the implementer's prompt.

## Step 1 — Interactive story capture (you do this directly, not a subagent)

1. Check `git status`. If the working tree is not clean, stop and tell the user — uncommitted changes would ride along into the feature branch and corrupt what the reviewer sees as "the diff." Ask them to commit or stash first.
2. Ask the user about the feature request. This is the *only* point in the whole pipeline where any interaction happens. Ask about the big and medium decisions — scope, behavior, what happens on edge cases that matter to the product, how it should integrate with existing features — using `AskUserQuestion` where there's a genuine decision to make. Do not ask about small implementation details (naming, exact error messages, minor edge-case handling) — leave those for the planner to decide and log.
3. Keep asking/clarifying until you have enough to write an unambiguous story. Then write the finalized story as a detailed feature story (user-facing behavior, acceptance criteria, explicit non-goals if relevant) — not an implementation plan.
4. Pick a short kebab-case slug for the feature (e.g. `user-avatar-upload`). Write the story to `.claude/features/<slug>/story.md`.
5. Create and check out a new branch `feature/<slug>` from the current HEAD.

## Step 2 — Automated pipeline (zero interaction from here on)

Once the story file is written and the branch is checked out, call the `Workflow` tool with `name: "feature-pipeline"` and `args: { story: <the full story text> }`. This is the explicit, deliberate trigger for that workflow — always invoke it this way for this skill, never build the plan/implement/review loop yourself by hand.

The workflow runs entirely in the background. Wait for its completion notification — do not ask the user anything, do not poll, and do not improvise extra steps while it runs.

## Step 3 — Report back

When the workflow returns `{ approved, cycles, finalPlan, outstandingIssues, choices }`:

1. Write `.claude/features/<slug>/report.md` containing: approved status, number of cycles used, the full `choices` list (grouped by cycle, each with issue/decision/rationale), and `outstandingIssues` if not approved.
2. Tell the user directly, concisely:
   - Whether it was approved, and after how many cycles.
   - The branch name and that changes are sitting **uncommitted** on it — do not commit or push them yourself.
   - The full list of judgment calls the planner made in place of asking the user, so they can override any of them.
   - If not approved after 4 cycles: say so plainly, list the outstanding issues, and make clear the diff was left as-is rather than reverted, since it needs human judgment now.
