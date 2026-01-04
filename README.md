# Pathery solver

This repository contains a system that proposes solutions to a limited variant of the game [Pathery](www.pathery.com). It is not affiliated with Pathery, and the problem it solves is a subset of the complete Pathery rules.

## Game description

A snake exists on a rectangular grid. It begins at one of the checkpoints tied for the lowest number, then makes its way to a checkpoint tied for the next lowest number, until it reaches a checkpoint with every number. Player solutions are scored by the number of steps the snake takes.

The snake moves one step at a time, either up, right, down, or left (but not diagonally). If multiple checkpoints have the lowest value, the snake starts at the one which yields the lowest score. If all routes are equal, the snake prefers to go up, then right, then down, then left.

The snake cannot step on tiles with walls. The system places some immovable walls, and the player has a finite number of additional walls they may add to delay the snake. If the snake steps on a teleport-in tile, it exhausts that tile so it may not be activated again, then emerges at the corresponding teleport-out tile without consuming a step.
