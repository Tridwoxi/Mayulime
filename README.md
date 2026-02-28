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
> Specifically, it lacks support for duplicate checkpoints (including start and finish), and any
> special tile aside from checkpoints and teleports.

The world is a rectangular grid of empty cells, player walls, system walls, teleport-in/out pairs,
and uniquely-numbered checkpoints. Initially, the world does not contain player walls.
The player may convert up to some number of empty cells to player walls.

A snake makes its way from each checkpoint to the next in ascending order. It moves one step at a
time either up, right, down, or left (but not diagonally). The snake takes a shortest path. Among
shortest paths, the Snake prefers to go up, then right, then down, then left.

If the snake visits a teleport-in it has not yet visited, it emerges at the corresponding
teleport-out. From there, it continues its journey. The snake does not account for teleports when
calculating a shortest path.

The snake cannot visit cells with system walls or player walls on them. The player's goal is to
maximize the total number of steps the snake takes. If the snake fails to reach a checkpoint, the
player loses.

## Related work

You may also be interested in:

- https://github.com/t3chn0l0g1c/pathery
- https://github.com/WuTheFWasThat/midnighttherapy
- https://github.com/bwoodbury3/pathery-solver
- https://github.com/pitrack/m-pathery
