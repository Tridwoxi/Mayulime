# Pathery solver

This repository contains a system that proposes solutions problems from the game [Pathery](https://www.pathery.com).

This repository is not affiliated with Pathery. For ease of programming, it uses a simplified variant of the complete Pathery rules.

## Game description

The world is a rectangular grid of open cells, rubbers, bricks, and uniquely-numbered checkpoints. The world is initially devoid of rubbers. The player must convert a given number of open cells to rubber cells.

A snake makes its way from each checkpoint to the next in ascending order. It moves one step at a time either up, right, down, or left (but not diagonally). The snake cannot visit cells with bricks or rubbers on them. The snake takes the shortest path.

The player's goal is to maximize the number of steps the snake takes. If the snake cannot reach a checkpoint, the player loses.
