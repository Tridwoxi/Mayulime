# Agent instructions

The game "Pathery" is an exercise in creating the longest shortest path on a maze by placing a set
of walls. The full problem statement is in the README. This repository contains an automated Pathery
solver.

You own the GUI. Treat it as your codebase. Redesign it, replace patterns, and change contracts as
needed to improve it. Do not limit yourself to minimal edits there. Outside of the GUI, you are a
guest, so should prefer precise changes and avoid opportunistic refactors.

## Testing

If you edit code, execute `./gradlew build` to compile and test. If you cannot do so because the
Gradle wrapper or jar is missing, ask the human to create it. Do not attempt to launch the
application itself unless explicitly asked.

If Checkstyle or Spotbugs complains during the build, evaluate if fixing the violations would
improve or harm the design. Then either fix it or keep whatever you originally wrote and inform the
human instead.

## Package structure

Application packages are in `src/main/java/`:

`infra.gui` - JavaFX GUI.
`infra.launch` - Application launch point.
`infra.output` - Logging.

`think.common` - Shared backend classes.
`think.domain` - Problem model and codec.
`think.manager` - Solver orchestration and integration.
`think.solvers` - Solver implementations and support.

Test packages are in `src/test/java/`:

`e2e` - End-to-end and integration tests.
`unit` - Unit tests grouped by package. Only public objects are tested.

## Style

Use descriptive variable names. Abbreviation is acceptable when meaning is clear. Avoid one letter
variable names.

Repo code may receive `null` from library code, pass `null` to library code, and use `null` for
private implementation details. Repo subroutines must never accept `null`, return `null`, or let
`null` escape into other repo code. Hence there is no need to check for `null` in repo code.

If an interface already exists, accept and return that interface rather than the implementation. If
no interface already exists, just use the implementation.

Extending or implementing custom classes and interfaces is discouraged beyond one layer deep.
