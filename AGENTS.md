# Agent instructions

The game "Pathery" is an exercise in creating the longest shortest path on a maze by placing a set of walls. This repository contains an automated Pathery solver.

If you need information about architecture (including coding conventions), the problem
statement, or the theory of this project, consult `docs/`.

Do not write documentation unless asked. Write comments as needed. If you stumble upon
lies (either in the docs or in Javadoc), please inform the human.

If you edit code, test the project by running the following command:

```sh
# Run the project headless on the simple example for 3 seconds.
./gradlew runHeadless --args="examples/simple.mapcode 3"
```

If Checkstyle or Spotbugs complains during the build, fix violations. If the violations
are difficult to fix, it's probably not your fault; inform the human instead.

If the gradle wrapper or jar is not available, ask the human to create it.
