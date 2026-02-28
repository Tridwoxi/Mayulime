# Agent instructions

The game "Pathery" is an exercise in creating the longest shortest path on a maze by placing a set
of walls. The full problem statement is in the README. This repository contains an automated Pathery
solver.

Do not write documentation unless explicitly asked. Write comments as needed. If you stumble upon
lies in the documentation, please inform the human.

## Testing

If you edit code, execute `./gradlew test` to run all the tests. If you cannot do so because the
Gradle wrapper or jar is missing, ask the human to create it. Do not attempt to launch the
application itself unless explicitly asked.

If Checkstyle or Spotbugs complains during the build, evaluate if fixing the violations would
improve or harm the design. Then either fix it or keep whatever you originally wrote and inform the
human instead.

## Repository layout

Application code lives in `src/main/java/`. `infra/` contains infrastructure (gui, logging, launch
point). `think/` contains the backend. The backend is linked to the frontend by the Manager.

The backend is split across `domain/`, `graph/`, and `solve/`. Domain does MapCode parsing in
`codec/` and problem representation in `repr/`. Graphs have implementations in `impl/` and
algorithms in `algs/`. Solvers have strategies in `stra/` and non-graph tools in `tools/`.

Tests live in `src/test/java/`. Within tests, `e2e/` contains end-to-end and integration tests, and
`unit/` contains unit tests grouped by package.

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
