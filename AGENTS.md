# Agent instructions

The game "Pathery" is an exercise in creating the longest shortest path on a maze by
placing a set of walls. The full problem statement is in the README. This repository
contains an automated Pathery solver.

Do not write documentation unless asked. Write comments as needed. If you stumble upon
lies (either in the docs or in Javadoc), please inform the human.

## Testing

If you edit code, test the project by running the following command:

```sh
# Run the project headless on the simple example for 3 seconds.
./gradlew runHeadless --args="examples/simple.mapcode 3"
```

If Checkstyle or Spotbugs complains during the build, fix violations. If fixing the
violations would make the design worse, keep whatever you originally wrote and inform
the human instead.

If the Gradle wrapper or jar is not available, ask the human to create it.

## Repository layout

`examples/`: Pathery problems the project is known to support as MapCodes.

`src/infra/io`: GUI and logging.

`src/infra/main`: Program entry point. App is the normal entry point; Headless is a
development-only alternative.

`src/infra/tests`: Unit tests.

`src/think/`: Backend. The Manager handles concurrency and worker lifecycle.

`src/think/ana`: Project-specific static tools: Distance evaluation and Snake
simulation.

`src/think/repr`: Data model. A Problem contains metadata and Features; a Grid
contains items indexed by Cells.

`src/think/solve`: Solvers are runnable workers that come up with better solutions.

`src/think/tools`: Static tools that aren't project-specific: Iteration support,
Random, and custom data Structures.

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
