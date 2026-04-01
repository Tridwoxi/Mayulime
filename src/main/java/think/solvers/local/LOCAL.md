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

**Ruin builds on Intersect**

Rather than restart, clear a random rectangle from the maze. Let width, height, top, and left be
chosen uniformly from the full maze such that it can wrap around edges.

Extensive testing shows if you don't wrap around edges, the difference is noise.

I imagine Ruin to be the hill climber of hill climbers (so is not a pure RRHC: througput not
comparable, and not independent of time). Very effective. Possible improvement is to maintain a
frontier of bests.

Ruin sometimes does 150000 or 350000 throughput on medium1. In these cases, it finds a poor local
optimum and cannot 1-opt its way out. Since no seed happens, it is never able to improve score once
it gets stuck. Hence, unlike its predecessors, sometimes chokes on medium1.

**Anneal**

Simulated annealing, but temperature never decreases. Make random moves then tend to accept good
ones.

## Benchmarks and profiles

<!-- For consistency, whole benchmark suite must come from same run, so updates should replace
everything. This doesn't apply to profiles unless drastic changes to environment happen. -->

**Mean throughput (1 thread, 1 second, 3 samples)**

| Solver     | small1 | medium1 | large1 | huge1 |
| ---------- | ------ | ------- | ------ | ----- |
| Climb      | ~3900  | ~840    | ~130   | ~5    |
| Identity   | ~5000  | ~1600   | ~200   | ~10   |
| Walk       | ~1200  | ~190    | ~39    | ~1    |
| Chokepoint | ~15100 | ~5200   | ~930   | ~56   |
| Uncover    | ~11000 | ~4100   | ~850   | ~73   |
| Intersect  | ~13000 | ~4600   | ~1100  | ~82   |
| Ruin       | ~21100 | ~5500   | ~1200  | ~97   |

**Median score (1 thread, 300 milliseconds, 10 samples)**

| Solver         | small1 | medium1 | large1 | huge1 |
| -------------- | ------ | ------- | ------ | ----- |
| Climb          | =43    | =84     | ~167   | ~283  |
| Identity       | =43    | =84     | ~170   | ~342  |
| Walk (1000 ms) | =43    | =84     | ~185   | ~387  |
| Chokepoint     | =43    | =84     | ~178   | ~371  |
| Uncover        | =43    | =84     | ~175   | ~388  |
| Intersect      | =43    | =84     | ~179   | ~397  |
| Ruin           | =43    | ~84     | ~185   | ~403  |

**Profile (async-profiler, 3 seconds CPU mode)**

| Solver     | huge1                                           |
| ---------- | ----------------------------------------------- |
| Climb      | ~99% rearrangeWalls                             |
| Identity   | ~98% rearrangeWalls, ~1% getCellsOnShortestPath |
| Walk       | ~99% rearrangeWalls                             |
| Chokepoint | ~95% rearrangeWalls, ~4% getChokepoints         |
| Uncover    | ~85% rearrangeWalls, ~15% getChokepoints        |
| Intersect  | ~96% rearrangeWalls, ~4% getChokepoints         |
| Ruin       | ~97% rearrangeWalls, ~3% getChokepoints         |
