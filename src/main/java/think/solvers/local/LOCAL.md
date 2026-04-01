# Local search

This package is home to the local state space search solver family. Please copy and paste solvers
instead of using inheritance because solvers need to evolve independently; write with deletion
(instead of extension) in mind. Do not edit a solver once another solver builds on it.

## Lineage

**Climb**

Random restart hill climber. Seed the maze with a varying number of walls. Improve score by placing
or moving a single wall.

Given the neighbor set "place one wall (subject to budget)" and "move one wall", all proposals are
locally optimal by brute force check.

Instant on small, effective for medium to large.

**Identity builds on Climb**

If a cell is not on a shortest path, blocking it cannot increase score. A cell $v$ is on a
shortest path iff $d(s, v) + d(v, f) = d(s, f)$.

Speedup comes from evaluating fewer states.

**Walk builds on Climb**

Before the expensive O(n^3) exhaustive rearrange step, let's run the same thing many times but also
accept neutral moves.

Weirdly good (on par with Chokepoint!) after a bugfix; can be used as a desparation measure to
fiddle with local optima.

**Chokepoint builds on Identity**

A chokepoint is a cell that is the only cell at its distance layer on a shortest path segment. Every
shortest path must pass through it, so blocking it is guaranteed to increase path length or
disconnect.

The smaller state set helps a lot but is only effective against open regions.

**Uncover builds on Chokepoint**

A similar move set to Chokepoint is to remove a player wall first then compute chokepoints. If it
is possible to increase score, then it must be done by placing that wall back down onto a newly
uncovered chokepoint.

No meaningful difference.

**Intersect builds on Chokepoint, Uncover**

The exact move set necessary to confirm a local optimum, methinks, is the intersection of what
Chokepoint and Uncover says it is. Both of those are locally optimal, so their symmetric difference
is wasted compute.

Solid improvement over predecessors on big maps.

Future work in the pure 1-opt hill climbing sphere should attempt heuristic pruning strategies and
getting rearrangeWalls to run in O(n^2), omg I want this so bad.

**Anneal**

Simulated annealing, but temperature never decreases. Make random moves then tend to accept good
ones.

## Benchmarks and profiles

<!-- For consistency, whole benchmark suite must come from same run, so updates should replace
everything. This doesn't apply to profiles unless drastic changes to environment happen. -->

**Throughput (1 thread, 1 second, 1 sample)**

| Solver     | small1 | huge1 |
| ---------- | ------ | ----- |
| Climb      | ~3800  | ~4    |
| Identity   | ~5000  | ~11   |
| Walk       | ~1200  | ~1    |
| Chokepoint | ~14800 | ~51   |
| Uncover    | ~10900 | ~68   |
| Intersect  | ~12900 | ~80   |

**Median score (1 thread, 300 milliseconds, 10 samples)**

| Solver         | huge1 |
| -------------- | ----- |
| Climb          | ~286  |
| Identity       | ~336  |
| Walk (1000 ms) | ~393  |
| Chokepoint     | ~385  |
| Uncover        | ~384  |
| Intersect      | ~395  |

**Profile (async-profiler, 3 seconds CPU mode)**

| Solver     | huge1                                           |
| ---------- | ----------------------------------------------- |
| Climb      | ~99% rearrangeWalls                             |
| Identity   | ~98% rearrangeWalls, ~1% getCellsOnShortestPath |
| Walk       | ~99% rearrangeWalls                             |
| Chokepoint | ~95% rearrangeWalls, ~4% getChokepoints         |
| Uncover    | ~85% rearrangeWalls, ~15% getChokepoints        |
| Intersect  | ~96% rearrangeWalls, ~4% getChokepoints         |
