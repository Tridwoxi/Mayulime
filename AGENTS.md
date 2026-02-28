# Agent instructions

The game "Pathery" is an exercise in creating the longest shortest path on a maze by placing a set
of walls. The full problem statement is in the README. This repository contains an automated Pathery
solver.

Do not write documentation unless asked. Write comments as needed. If you stumble upon lies in the
documentation, please inform the human.

## Testing

If you edit code, execute `./gradlew test` to run all the unit tests. If you cannot do so because the Gradle wrapper or jar is missing, ask the human to create it.

If Checkstyle or Spotbugs complains during the build, evaluate if fixing the violations would
improve or harm the design. Then either fix it or keep whatever you originally wrote and inform the
human instead.

## Repository layout

<!-- THE FOLLOWING IS OUTDATED AND THE HUMAN WILL REWRITE IT EVENTUALLY -->

`examples/`: Pathery problems the project is known to support as MapCodes.

`src/main/java/infra/io`: GUI and logging.

`src/main/java/infra/main`: Program entry point. App is the normal entry point; Headless is a
development-only alternative.

`src/main/java/think/`: Backend. The Manager handles concurrency and worker lifecycle.

`src/main/java/think/ana`: Project-specific static tools: Distance evaluation and Snake simulation.

`src/main/java/think/repr`: Data model. A Problem contains metadata and hands out blank
Solutions for solvers to work on. A Grid contains items indexed by Cells.

`src/main/java/think/solve`: Solvers are runnable workers that come up with better solutions.

`src/main/java/think/tools`: Static tools that aren't project-specific: Iteration support, Random,
and custom data Structures.

`src/test/java/all`: Unit tests.

## Style

A functional approach is encouraged but not required. Consider if passing functions would help.

Use descriptive variable names. Abbreviation is acceptable when meaning is clear. Avoid one letter
variable names.

Currently, constant-factor performance improvements are not a priority. The comment "PERF:"
indicates areas for work if performance improvements are desired.

Null pointers may be produced and checked for, but `null` is never accepted or passed by any
subroutine defined in this project.

If an interface already exists, prefer to accept and return that interface rather than the
implementation. Avoid raw arrays unless the caller is passing one directly.

Extending or implementing custom classes and interfaces is discouraged beyond one layer deep.
