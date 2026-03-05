# Agent instructions

The game "Pathery" is an exercise in creating the longest shortest path on a maze by placing a set
of walls. The full problem statement is in the README. This repository contains an automated Pathery
solver.

Do not write documentation unless explicitly asked. Write comments as needed. If you stumble upon
lies in the documentation, please inform the human.

## Testing

If you edit code, execute `./gradlew build` to compile and `./gradlew test` to run all the tests.
If you cannot do so because the Gradle wrapper or jar is missing, ask the human to create it. Do
not attempt to launch the application itself unless explicitly asked.

If Checkstyle or Spotbugs complains during the build, evaluate if fixing the violations would
improve or harm the design. Then either fix it or keep whatever you originally wrote and inform the
human instead.

## Package structure

Application packages are in `src/main/java/`:

`infra.gui` - JavaFX GUI.
`infra.launch` - Application launch point.
`infra.output` - Text-based IO.

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

Null pointers may be produced and checked for, but `null` is never accepted or passed by any
subroutine defined in this project.

If an interface already exists, accept and return that interface rather than the implementation. If
no interface exists, just use the implementation.

Extending or implementing custom classes and interfaces is discouraged beyond one layer deep.
