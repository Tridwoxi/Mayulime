# Mayulime

The game [Pathery](https://www.pathery.com) is an exercise in creating the longest
shortest path on a maze by placing a set of walls.

The system in this repository proposes solutions to Pathery problems, so may be helpful
as an assistant or checker. This repository is not affiliated with Pathery.

The `docs/` folder contains more information about architecture, the problem statement,
and theory. These are helpful for development, but are not needed for usage.

## Usage

Install the [Gradle](https://gradle.org) build tool, version 7 or later. Walk through
the steps to create the wrapper, and once you have it, run the project. Gradle
is supposed to install the required dependency ([JavaFX](https://openjfx.io)) and
optional dependencies ([Checkstyle](https://checkstyle.org),
[Spotbugs](https://spotbugs.github.io)) automatically. If it doesn't work, message the
author of this project.

When run, the project launches a GUI with a button for a file selector. Upload a file
with extension `.mapcode` that this project supports, and wait for it to display its
solutions. Supported files in the Pathery [MapCode](https://www.pathery.com/mapeditor)
format can be found in `examples/`.

## Related work

You may also be interested in:

- https://github.com/t3chn0l0g1c/pathery
- https://github.com/WuTheFWasThat/midnighttherapy
- https://github.com/bwoodbury3/pathery-solver
- https://github.com/pitrack/m-pathery
