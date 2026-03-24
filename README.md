# Mayulime

The game [Pathery](https://www.pathery.com) is an exercise in creating the longest shortest path on
a maze by placing a set of walls. The system in this repository proposes solutions to a proper
subset of Pathery problems, so it may be helpful as an assistant or checker. This repository is not
affiliated with Pathery.

## Usage / development

Install [JDK](https://jdk.java.net) 25 or later and [Gradle](https://gradle.org) 9.1 or later. In
the repository root, execute `gradle run`. Gradle will install the dependencies
[JavaFX](https://openjfx.io), [LMAX Disruptor](https://lmax-exchange.github.io/disruptor/), and
[picocli](https://picocli.info). It may also configure [Spotbugs](https://spotbugs.github.io),
[Checkstyle](https://checkstyle.org), [JUnit](https://junit.org), and
[JMH](https://openjdk.org/projects/code-tools/jmh/). If it doesn't work, message me.

When run, the project launches a GUI. Follow the on-screen instructions to upload a Pathery
[MapCode](https://www.pathery.com/mapeditor), and wait for solutions. Supported examples are in
`examples/`.

## Problem statement

> For ease of programming, this project uses a simplified variant of the complete Pathery rules.
> Teleports are not supported, but there are plans for them. No other features are planned.

The world is a grid graph with some vertices missing. Of the remaining vertices, some are
checkpoints, and the rest are empty. Checkpoints are well-ordered and unique. The player has a
budget to remove empty vertices by placing walls. When the player is done removing vertices, their
score is the sum of pairwise shortest path lengths between checkpoints. If there is no shortest
path between checkpoints, the player loses.

## Related work

You may also be interested in:

- [t3chn0l0g1c/pathery](https://github.com/t3chn0l0g1c/pathery)
- [WuTheFWasThat/midnighttherapy](https://github.com/WuTheFWasThat/midnighttherapy)
- [bwoodbury3/pathery-solver](https://github.com/bwoodbury3/pathery-solver)
- [pitrack/m-pathery](https://github.com/pitrack/m-pathery)
