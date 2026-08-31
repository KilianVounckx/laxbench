# Coding conventions

This is a Kotlin Multiplatform project (`shared`, `webApp`). Web (Wasm) is currently the only
target being built — Android, iOS, and Desktop (JVM) were removed to unblock CI and will likely
return later. This does not relax convention 1 below: the "put it in `commonMain`" rule exists to
keep the codebase ready to grow more targets cheaply, not because there happen to be several right
now, so it applies exactly as strictly with one target as it will once more come back.

## 1. Maximize common code

This is the highest-priority structural convention in this codebase: as much code as possible must
live in `shared/src/commonMain`, not in a platform source set (currently just `wasmJsMain`; also
`androidMain`, `iosMain`, `jvmMain`, `jsMain` when those targets return) or a platform app module
(currently just `webApp`; also `androidApp`, `desktopApp` when those targets return). Default to
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

## 3. Enums over boolean flags

Prefer a custom enum over a boolean parameter, property, or field almost every time, even when
there are currently only two possible states. A boolean's meaning depends entirely on its name and
reads only as well as the call site does (`hidden = true`); a two-or-more-value enum documents
itself at every call site (`Visibility.HIDDEN`) and leaves room to grow a third state later without
resorting to a second boolean or a null-as-third-state hack. This applies equally to
constructor/function parameters, class properties, and stored fields.

A plain boolean is still fine when it's a genuine, self-explanatory yes/no answer with no
plausible third state and no ambiguity at the call site (e.g. a computed `isEmpty`, or a
`contains(...): Boolean` return value), or when a platform/library API you don't control mandates
one.

Not mechanically enforceable — checked in review against this file (see
`.claude/agents/feature-reviewer.md`).

## 4. Code reuse — avoid duplication

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

## 5. Tests

Every non-private function in the domain layer (`io.github.kilianvounckx.laxbench.domain`) must
have tests. Other code may be tested if it gets complicated enough to warrant it, but isn't
required to.

Previously enforced approximately via a Kover coverage-verification rule scoped to that package,
requiring 100% line coverage under `*.domain.*`. Kover only measures JVM-based targets, and the
`jvm()` target was removed along with Android/iOS/Desktop (see the top of this file), so that
mechanical check is currently disabled — `shared/build.gradle.kts` has no `kover` plugin or
`koverVerify` task. This convention is enforced by review only until a JVM-based (or
Kover-for-Wasm-capable) target comes back.

Run: `./gradlew :shared:wasmJsTest` to run the domain tests without coverage measurement.

## 6. Formatting

All Kotlin files (including `.gradle.kts` build scripts) are formatted with ktfmt, Google style.
Applied to every subproject via a `subprojects {}` block in the root `build.gradle.kts`; wired
into each module's `check` task automatically by the ktfmt plugin.

Run: `./gradlew ktfmtFormat` to format, `./gradlew ktfmtCheck` to verify without changing files.

## 7. No unused code

No unused variables, functions, parameters, or imports. Enforced by treating all Kotlin compiler
warnings as errors (`allWarningsAsErrors`, set for every subproject in the root `build.gradle.kts`)
— the compiler already warns on unused imports, variables, and private declarations, so this turns
those warnings into build failures. This does not catch unused *public* declarations no one calls
anywhere (the compiler can't know that); watch for that in review.

## 8. Dependency versions in the version catalog

Every Gradle dependency and plugin version must be declared in `gradle/libs.versions.toml`, never
as a literal in a `build.gradle.kts`. (`settings.gradle.kts` is exempt: the `pluginManagement`
block resolves before the version catalog is available, so the
`foojay-resolver-convention` plugin version there is unavoidable.)

Enforced by a custom `checkDependencyVersionCatalogUsage` task (root `build.gradle.kts`) that fails
if any `build.gradle.kts` contains a hardcoded `group:artifact:version` coordinate or a
`id(...) version "..."` plugin declaration.

## Git workflow

Never run git commands that change repository state — `commit`, `push`, `reset`, `stash`,
`checkout`/`restore` that discards changes, staging/unstaging, etc. The user does all committing
and pushing themselves. Read-only git commands (`status`, `diff`, `log`, `show`) are fine and
expected. If a task would normally require committing or stashing to proceed (e.g. needing a clean
working tree), stop and ask the user to do it themselves, then continue once they confirm.

## Running everything

`./gradlew check` runs ktfmtCheck, the version-catalog check, and (per module) compilation with
warnings as errors, across every subproject. Domain test coverage is not currently checked
mechanically (see convention 5) — run `./gradlew :shared:wasmJsTest` to run the tests themselves.
Note: `:shared:wasmJsBrowserTest` (part of `check`) needs a headless browser (Chrome/Firefox); in a
sandbox without one installed, it will fail even though everything else (compilation, ktfmt,
version-catalog check) passes — that's an environment gap, not a convention violation. GitHub
Actions' `ubuntu-latest` runner images ship browsers pre-installed, so this isn't an issue in CI.
