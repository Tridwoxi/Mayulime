# Local search

This package is home to the local state space search solvers. Since I don't know what I'm doing,
this package looks more like a big experiment with lots of solvers that differ from each other
rather than a set of distinct ones. One should copy and paste solvers instead of using inheritance
because solvers need to evolve independently. A little bit of code duplication isn't fatal.

## Solvers

ClimbV1Solver is a basic random restart hill climber. It seeds the board with a varying number of
walls then improves the score by placing or moving a single wall. Its tactics should be common to
all climbers.

## Benchmarks

<!--TODO: Maybe time to actually write Bench.java, eh?-->
