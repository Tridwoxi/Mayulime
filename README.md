# Tridwoxi's Pathery AI

This repository contains a system that proposes solutions to problems from the game
[Pathery](https://www.pathery.com). This repository is not affiliated with Pathery. For
ease of programming, it uses a simplified variant of the complete Pathery rules.

## Task specification

The world is a rectangular grid of empty cells, player walls, system walls,
teleport-in/out pairs, and uniquely-numbered checkpoints. Initially, the world does not
contain player walls. The player may convert up to some number of empty cells to player
walls.

A snake makes its way from each checkpoint to the next in ascending order. It moves one
step at a time either up, right, down, or left (but not diagonally). The snake takes a
shortest path. Among shortest paths, the Snake prefers to go up, then right, then down,
then left.

If the snake visits a teleport-in it has not yet visited, it emerges at the
corresponding teleport-out. From there, it continues its journey. The snake does not
account for teleports when calculating a shortest path.

The snake cannot visit cells with system walls or player walls on them. The player's
goal is to maximize the total number of steps the snake takes. If the snake fails to
reach a checkpoint, the player loses.

## Architecture

The system is a JavaFX Application. The launch point, tests, and GUI live in `src/app/`.
When the user uploads a problem, it is passed to the manager in `src/think/`.

The manager concurrently runs strategies located in `src/think/stra/`. The problem
representation and related components live in `src/think/repr/`. Pathfinding and
analysis is done by `src/think/ana/`. Utilities live in `src/think/tools/`.

Supported examples in the Pathery [MapCode](https://www.pathery.com/mapeditor) format
can be found in `examples/`. Use the [Gradle](https://gradle.org) build tool,
configured in `build.gradle`, to run.
