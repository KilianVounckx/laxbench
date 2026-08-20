---
name: feature
description: Build a feature end-to-end through an isolated agent pipeline — interactive story capture, then a fully automated, non-interactive plan/implement/review loop (max 4 cycles) run via the feature-pipeline workflow. Use when the user asks to build, add, or implement a feature via this pipeline (e.g. "/feature", "build this feature", "run the feature pipeline for X").
---

This skill runs a feature from request to reviewed diff through five isolated stages: story capture (interactive, done by you directly), then plan, implement, collect, review (all automated, no interaction, up to 4 retry cycles of plan/implement/collect/review), orchestrated by the `feature-pipeline` workflow in `.claude/workflows/feature-pipeline.js` using the subagents defined in `.claude/agents/feature-planner.md`, `feature-implementer.md`, `feature-diff-collector.md`, and `feature-reviewer.md`.

**Isolation is the point of this design**: the planner, implementer, collector, and reviewer subagents never see each other's reasoning or communicate directly. They only ever receive exactly what the workflow script passes them — the story, the plan, or the collected diff/check output — never anything else. Do not weaken this by, e.g., pasting the plan into the reviewer's prompt, or the story into the implementer's prompt.

**The planner and reviewer have zero write capability, structurally, not just by instruction**: both are limited to `Read, Grep, Glob` — no `Bash`, no `Edit`, no `Write` — so there is no tool available to either of them that could modify a file even by accident. This is why the reviewer doesn't run `git diff`/`./gradlew check` itself: a dedicated `feature-diff-collector` agent (Bash-capable, but purely mechanical — it runs a fixed set of commands and reports raw output, no judgment) does that independently after the implementer finishes, and the reviewer gets the results handed to it. This also means the reviewer's information doesn't depend on the implementer's honesty about its own build status. Do not "simplify" this by giving the reviewer Bash back — scoped/parameterized Bash grants like `Bash(git diff:*)` in a subagent's `tools:` field do NOT actually restrict which commands can run in this harness (confirmed empirically: a subagent granted only `Bash(echo hi)` was still able to run arbitrary other commands) — the only real restriction is not granting Bash at all.

**Model tiers are deliberately uneven**: the implementer runs on a cheap, low-effort model (`haiku`, `effort: 'low'`) because it isn't supposed to reason — it just has to follow the plan literally. The planner and reviewer run at high effort on the inherited (more capable) model, since they do the actual judgment calls: working out implementation details/edge cases, and catching inconsistencies. Don't "fix" the implementer by upgrading its model if it seems to be struggling — that's a signal the plan wasn't precise enough, and the fix belongs in the planner's prompt or the plan it produces, not in giving the implementer more reasoning budget.

**No branches, no artifact files by default**: this pipeline works directly on the current branch (normally `main`) and never writes the story or the final report to disk. Hold the story text in memory (as part of your own turn) and pass it straight to the `Workflow` call — don't stage it through a repo file first. Only create a dedicated branch, or persist the story/report as files, if the user explicitly asks for that in this run.

## Step 1 — Interactive story capture (you do this directly, not a subagent)

1. Check `git status`. If the working tree is not clean, stop and tell the user — uncommitted changes would ride along into the diff and corrupt what the reviewer sees as "the diff." Ask them to commit or stash first.
2. Make sure you're on `main` (`git checkout main`) unless the user explicitly asked to work on a specific different branch for this feature — do not create a new branch yourself.
3. Ask the user about the feature request. This is the *only* point in the whole pipeline where any interaction happens. Ask about the big and medium decisions — scope, behavior, what happens on edge cases that matter to the product, how it should integrate with existing features — using `AskUserQuestion` where there's a genuine decision to make. Do not ask about small implementation details (naming, exact error messages, minor edge-case handling) — leave those for the planner to decide and log.
4. Keep asking/clarifying until you have enough to write an unambiguous story. Compose the finalized story as a detailed feature story (user-facing behavior, acceptance criteria, explicit non-goals if relevant) — not an implementation plan. Keep it in memory; don't write it to a file.

## Step 2 — Automated pipeline (zero interaction from here on)

Once the story is finalized, call the `Workflow` tool with `name: "feature-pipeline"` and `args: { story: <the full story text> }`. This is the explicit, deliberate trigger for that workflow — always invoke it this way for this skill, never build the plan/implement/review loop yourself by hand.

The workflow runs entirely in the background. Wait for its completion notification — do not ask the user anything, do not poll, and do not improvise extra steps while it runs.

## Step 3 — Report back

When the workflow returns `{ approved, cycles, finalPlan, outstandingIssues, choices }`, tell the user directly in chat — no report file:

- Whether it was approved, and after how many cycles.
- That changes are sitting **uncommitted** on the current branch — do not commit or push them yourself.
- The full list of judgment calls the planner made in place of asking the user, so they can override any of them.
- If not approved after 4 cycles: say so plainly, list the outstanding issues, and make clear the diff was left as-is rather than reverted, since it needs human judgment now.
