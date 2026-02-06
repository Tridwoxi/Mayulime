# Problem statement

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

## Note

For ease of programming, this project uses a simplified variant of the complete Pathery
rules. In particular, it lacks support for duplicate checkpoints (including start and
finish), and any special tile aside from checkpoints and teleports.
