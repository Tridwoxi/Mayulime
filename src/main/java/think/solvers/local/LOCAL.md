# Local search

This package is home to the local state space search solver family. Please copy and paste solvers
instead of using inheritance because solvers need to evolve independently. Do not edit a solver
once another solver builds on it.

## Lineage

**Climb**

Random restart hill climber. Seed the board with a varying number of walls. Improve score by
placing or moving a single wall.

Given the neighbor set "place one wall (subject to budget)" and "move one wall", all proposals are
locally optimal by brute force check.

Instant on small, effective for medium to large.

**Identity builds on Climb**

If a vertex is not on a shortest path, blocking it cannot increase score. A vertex $v$ is on a
shortest path iff $d(s, v) + d(v, f) = d(s, f)$.

Speedup comes from evaluating fewer candidates.

**Walk builds on Climb**

Before the expensive O(n^3) exhaustive rearrange step, let's run the same thing many times but also
accept neutral moves.

Not promising. Throughput on huge1 is weirdly high relative to small1. I don't know why.

**Chokepoint builds on Identity**

A chokepoint is a cell that is the only cell at its distance layer on a shortest path segment. Every
shortest path must pass through it, so blocking it is guaranteed to increase path length or
disconnect.

The smaller candidate set helps a lot but is only effective against open regions.

## Benchmarks and profiles

**Throughput (1 thread, 1 second, 1 sample)**

| Solver     | small1 | huge1 |
| ---------- | ------ | ----- |
| Climb      | ~3800  | ~7    |
| Identity   | ~4800  | ~9    |
| Walk       | ~2500  | ~22   |
| Chokepoint | ~14700 | ~54   |

**Median score (1 thread, 300 miliseconds, 10 samples)**

| Solver     | huge1 |
| ---------- | ----- |
| Climb      | 319   |
| Identity   | 345   |
| Walk       | 97    |
| Chokepoint | 374   |

**Profile (async-profiler, 5 seconds CPU mode)**

| Solver     | huge1                                           |
| ---------- | ----------------------------------------------- |
| Climb      | ~99% rearrangeWalls                             |
| Identity   | ~97% rearrangeWalls, ~2% getCellsOnShortestPath |
| Walk       | ?                                               |
| Chokepoint | ~97% rearrangeWalls, ~2% getChokepoints         |
