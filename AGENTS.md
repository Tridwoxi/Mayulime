# Agent instructions

The game "Pathery" is an exercise in creating the longest shortest path on a maze by placing a set
of walls. The full problem statement is in the README. This repository contains an automated Pathery
solver.

## Testing, benchmarking, and profiling

If you edit code, execute `./gradlew build` to compile and test. If you cannot do so because the
Gradle wrapper or jar is missing, stop and ask the human to create it. If Checkstyle or Spotbugs
complains during the build, evaluate if fixing the violations would improve or harm the design.
Then either fix it or suppress the warning.

If you need to benchmark a solver's performance, use `./gradlew bench` with similar settings to
other solvers unless told otherwise. When asked to benchmark score, many samples of shorter
duration is more reliable.

If you need to profile, use `scripts/profile.sh` when on Unix or Linux. If you cannot do so because
async-profiler is missing, ask the human to install it. Remember profiling is only a diagnostic,
and a slow method is not always an indication it needs to be made a constant factor faster.

If you decide to use third-party Python packages, install them into a throwaway venv in `tmp/`,
not directly into the human's system.

## Package structure

Some packages contain a markdown document inside them. These documents are required reading when
editing that package, but unnecessary otherwise.

Application packages are in `src/main/java/`:

`infra.bench` - Benchmarks.\
`infra.gui` - JavaFX GUI.\
`infra.launch` - Launch points.\
`infra.logging` - Logging.

`think.common` - Shared backend classes.\
`think.domain` - Problem model and codec.\
`think.ints` - Primitive int data structures.\
`think.manager` - Solver orchestration and integration.\
`think.solvers` - Solver implementations.

Test packages are in `src/test/java/`:

`e2e` - End-to-end and integration tests.\
`unit` - Unit tests grouped by package. Only public objects are tested.

## Style

Use descriptive variable names. Abbreviation is acceptable when meaning is clear. Avoid one letter
variable names.

Repo code may use `null` when interacting with library code or for private implementation details.
However, repo code must never accept, return, or let `null` escape into other repo code. Hence
there is usually no need to check.

If an interface already exists, accept and return that interface rather than the implementation. If
no interface already exists, use the implementation.

Extending or implementing custom classes and interfaces beyond one layer deep is permitted but
discouraged.

Prefer imported simple type names over fully-qualified names. For example, `List` via `import 
java.util.List` instead of `java.util.List` inline.
