# Naive solvers

This package is home to a family of naive solvers. To solve the simplest of maps, all you have to do
is not be stupid; unfortunately, the algorithms in this package are all stupid.

## BaselineSolver

BaselineSolver asks "is the best solution doing nothing?" and terminates. That is an interesting
approach philosophically, but mostly it's useful because the Manager uses it to send the first
status update.

## RandomSolver

RandomSolver samples all possible wall placements uniformly. Since it becomes brute force when run
long enough, it is a Las Vegas algorithm.

The number of wall placements $W$ is the number of ways to place up to blocking budget $B$ walls on $E$ empty cells:

$$
W = \sum_{i=0}^B {E \choose i}
$$

On a Pathery Simple, $N \approx 60$ and $S \approx 7$, so $W \approx 4 \times 10^8$. RandomSolver
and StandardEvaluator can guess around $1.3 \times 10^7$ times per second on 10 threads on my
computer, so it can solve the hardest of Simples in about 30 seconds. Usually, it will take less
time because Simples often have multiple optimal solutions. More difficult puzzles are out of reach.
