# Coding conventions

This is a Kotlin Multiplatform project (`shared`, `androidApp`, `desktopApp`, `webApp`). These
conventions apply to all code in it.

## 1. Maximize common code

This is the highest-priority structural convention in this codebase: as much code as possible must
live in `shared/src/commonMain`, not in a platform source set (`androidMain`, `iosMain`, `jvmMain`,
`jsMain`, `wasmJsMain`) or a platform app module (`androidApp`, `desktopApp`, `webApp`). Default to
writing new logic in `commonMain`. Only put something in a platform source set if it genuinely
cannot be expressed platform-independently — an actual platform API call behind an `expect`/`actual`
boundary — never because it was merely convenient to duplicate per platform. When an `expect`/`actual`
pair is unavoidable, keep both sides as small as possible and push everything that doesn't strictly
need the platform API back into `commonMain` around it.

Not mechanically enforceable — whether something *could* have been common is a judgment call, so
it's checked in review against this file (see `.claude/agents/feature-reviewer.md`): any code added
to a platform source set or app module should be treated with suspicion by default and justified by
a real platform constraint, not accepted just because that's where it was written.

## 2. Domain types

Anything that can be encapsulated in a domain type — a value with invariants, not just a bag of
data — must be. Prefer a small class/value class with a private constructor and a safe factory
(e.g. returning `Result`/nullable, or throwing at the single validated construction site) over
passing primitives or unchecked data classes around. Once a value exists, its invariants must hold
for its entire lifetime; don't re-validate the same thing at every call site.

Not mechanically enforceable — checked in review against this file (see
`.claude/agents/feature-reviewer.md`).

## 3. Code reuse — avoid duplication

Functional code — domain logic, business rules, general-purpose logic of any kind — must be
extracted to a single shared place rather than copy-pasted or reimplemented wherever it's needed
again. If two pieces of logic do the same thing, there should be one definition of that thing, not
two that have to be kept in sync by hand.

UI components should be extracted the same way when they truly represent the same thing repeated
in multiple places — this applies even to small pieces (e.g. a button style reused across screens),
not just large ones.

The judgment call that matters here is *sameness*, not resemblance. Larger UI sections in
particular often look alike without actually being the same thing — two screens that happen to
share a layout today but represent different concepts, or that will plausibly evolve independently.
Forcing those into one shared, parameterized component just because they currently look similar
usually produces something worse than the duplication it removes: a component riddled with
conditionals to handle cases that aren't really the same case. Extract when the two things should
change together; tolerate duplication when they merely look similar right now.

Not mechanically enforceable — "is this truly the same thing" is a judgment call, so it's checked
in review against this file (see `.claude/agents/feature-reviewer.md`).

## 4. Tests

Every non-private function in the domain layer (`io.github.kilianvounckx.laxbench.domain`) must
have tests. Other code may be tested if it gets complicated enough to warrant it, but isn't
required to.

Enforced approximately via a Kover coverage-verification rule scoped to that package
(`shared/build.gradle.kts`, requiring 100% line coverage under `*.domain.*`). This is a proxy, not
a guarantee of "one test per function" — untested branches of a covered function can still slip
through, so review still matters here too.

Run: `./gradlew :shared:koverVerify`

## 5. Formatting

All Kotlin files (including `.gradle.kts` build scripts) are formatted with ktfmt, Google style.
Applied to every subproject via a `subprojects {}` block in the root `build.gradle.kts`; wired
into each module's `check` task automatically by the ktfmt plugin.

Run: `./gradlew ktfmtFormat` to format, `./gradlew ktfmtCheck` to verify without changing files.

## 6. No unused code

No unused variables, functions, parameters, or imports. Enforced by treating all Kotlin compiler
warnings as errors (`allWarningsAsErrors`, set for every subproject in the root `build.gradle.kts`)
— the compiler already warns on unused imports, variables, and private declarations, so this turns
those warnings into build failures. This does not catch unused *public* declarations no one calls
anywhere (the compiler can't know that); watch for that in review.

## 7. Dependency versions in the version catalog

Every Gradle dependency and plugin version must be declared in `gradle/libs.versions.toml`, never
as a literal in a `build.gradle.kts`. (`settings.gradle.kts` is exempt: the `pluginManagement`
block resolves before the version catalog is available, so the
`foojay-resolver-convention` plugin version there is unavoidable.)

Enforced by a custom `checkDependencyVersionCatalogUsage` task (root `build.gradle.kts`) that fails
if any `build.gradle.kts` contains a hardcoded `group:artifact:version` coordinate or a
`id(...) version "..."` plugin declaration.

## Running everything

`./gradlew check` runs ktfmtCheck, the version-catalog check, and (per module) compilation with
warnings as errors, across every subproject. `./gradlew :shared:koverVerify` additionally checks
domain test coverage. Note: in a sandbox without an Android SDK configured, tasks that touch the
`androidApp`/Android target of `shared` will fail to configure regardless of these conventions —
that's an environment gap, not a convention violation.
