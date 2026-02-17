# Agent instructions

The game "Pathery" is an exercise in creating the longest shortest path on a maze by
placing a set of walls. The full problem statement is in the README. This repository
contains an automated Pathery solver.

Do not write documentation unless asked. Write comments as needed. If you stumble upon
lies in the documentation, please inform the human.

## Testing

If you edit code, test the project by running these commands:

```sh
# Run all the unit tests.
./gradlew test

# Run the project headless on the simple example for 1 second as an end-to-end test.
# The program is expected to achieve a score of at least 20.
./gradlew runHeadless --args="examples/simple.mapcode 1"
```

If Checkstyle or Spotbugs complains during the build, evaluate if fixing the violations
would improve or harm the design. Then either fix it or keep whatever you originally
wrote and inform the human instead.

If the Gradle wrapper or jar is not available, ask the human to create it.

## Repository layout

`examples/`: Pathery problems the project is known to support as MapCodes.

`src/main/java/infra/io`: GUI and logging.

`src/main/java/infra/main`: Program entry point. App is the normal entry point; Headless
is a development-only alternative.

`src/main/java/think/`: Backend. The Manager handles concurrency and worker lifecycle.

`src/main/java/think/ana`: Project-specific static tools: Distance evaluation and Snake
simulation.

`src/main/java/think/repr`: Data model. A Problem contains metadata and Features; a Grid
contains items indexed by Cells.

`src/main/java/think/solve`: Solvers are runnable workers that come up with better
solutions.

`src/main/java/think/tools`: Static tools that aren't project-specific: Iteration
support, Random, and custom data Structures.

`src/test/java/all`: Unit tests.

## Style

Use descriptive variable names. Abbreviation is acceptable but discouraged. One letter
names are prohibited.

Currently, constant-factor performance improvements are not a priority. The comment
"PERF:" indicates areas for work if performance improvements are desired.

Null pointers may be produced and checked for, but `null` is never accepted or passed
by any subroutine defined in this project.

Streaming and functional programming are encouraged but not required. Use ArrayLists
instead of raw arrays.

Extending or implementing custom classes and interfaces is permitted up to one layer
deep.
