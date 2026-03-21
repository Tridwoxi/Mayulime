# Local search

This package is home to the local state space search solvers. Since I don't know what I'm doing,
this package looks more like a big experiment with lots of solvers that differ from each other
rather than a set of distinct ones. One should copy and paste solvers instead of using inheritance
because solvers need to evolve independently. A little bit of code duplication isn't fatal.

## Solver lineage

ClimbV1Solver:

- Random restart hill climber.
- Seed the board with a varying number of walls.
- Improve score by placing or moving a single wall.
