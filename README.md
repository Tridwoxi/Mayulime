# Mayulime

The game [Pathery](https://www.pathery.com) is an exercise in creating the longest shortest path on
a maze by placing a set of walls. The system in this repository proposes solutions to a proper
subset of Pathery problems, so it may be helpful as an assistant or checker. This repository is not
affiliated with Pathery.

## Usage / development

Install version 7 or later of the [Gradle](https://gradle.org) build tool. In the repository root,
execute `gradle run`. Gradle will install the dependencies [JavaFX](https://openjfx.io),
[Checkstyle](https://checkstyle.org), and [Spotbugs](https://spotbugs.github.io), then run the
project. If it doesn't work, message the author of this project.

When run, the project launches a GUI with a button for a file selector. Upload a file with extension
`.mapcode` that this project supports, and wait for it to display its solutions. Supported files in
the Pathery [MapCode](https://www.pathery.com/mapeditor) format can be found in `examples/`.

## Problem statement

> For ease of programming, this project uses a simplified variant of the complete Pathery rules.
> Teleports are not supported, but there are plans for them. No other features are planned.

The world is a grid graph with some vertices missing. Of the remaining vertices, some are
checkpoints, and the rest are empty. Checkpoints are well-ordered and unique.

The player has a budget to remove empty vertices by placing walls. When the player is done removing
vertices, their score is the sum of pairwise shortest path lengths between checkpoints. If there is
no shortest path between checkpoints, the player loses.

## Related work

You may also be interested in:

- https://github.com/t3chn0l0g1c/pathery
- https://github.com/WuTheFWasThat/midnighttherapy
- https://github.com/bwoodbury3/pathery-solver
- https://github.com/pitrack/m-pathery
