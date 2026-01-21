# Tridwoxi's Pathery AI

This repository contains a system that proposes solutions to problems from the game [Pathery](https://www.pathery.com).

This repository is not affiliated with Pathery. For ease of programming, it uses a simplified variant of the complete Pathery rules.

## Task specification

The world is a rectangular grid of empty cells, player walls, system walls, and uniquely-numbered checkpoints. The world is initially devoid of player walls. The player must convert a given number of empty cells to player wall cells.

A snake makes its way from each checkpoint to the next in ascending order. It moves one step at a time either up, right, down, or left (but not diagonally). The snake cannot visit cells with system walls or player walls on them. The snake takes a shortest path.

The player's goal is to maximize the total number of steps the snake takes. If the snake cannot reach a checkpoint, the player loses.

## Architecture

The system is a JavaFX Application, which is launched from `src/app/Main.java`. Problems the user uploads are given to the manager.

`src/think/Manager.java` manages background threads to solve a problem. It uses strategies from `src/think/stra/` and runs indefinitely, occasionally sending the GUI updates.

Strategies read the representation of the problem and its features, which live in `src/think/repr/`. It applies analysis tools, which live in `src/think/ana`, to come up with better solutions.

Example problems can be found in `examples/`. Use the [Gradle](https://gradle.org) build tool, configured in `build.gradle`, to run.
