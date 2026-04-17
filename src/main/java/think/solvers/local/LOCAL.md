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

**Overfill builds on Ruin**

For rearrangeWalls, place the destination wall first even though this exceeds capacity by one, then
run distance fields once per waypoint segment. Unblocking any existing wall is now a single-cell
sensitivity query, so the whole rearrange step is O(n^2) when waypoint count is constant.

**Scramble builds on Overfill**

After ruining a rectangle, re-seed it. This fixes Ruin's regression on medium1. It comes at the
cost of not being as good. Overfill is better, but you need big sample sizes.

**Frontier builds on Overfill**

Fill a frontier of locally optimal states. Then, ruin a random frontier entry and continue from
there, and replace elements in the frontier if they are newer, better, and different according to
score and chokepoint set. This generalizes the single best of Overfill to a set, and patches the
symptom (unfortunately, only the symptom) of getting stuck in local optima seen in Ruin.

Significantly worse than Overfill at short durations (large effect at 1s on huge maps), but
converges by 10s and speculatively surpasses it for longer durations.

**Dump builds on Overfill**

In case of placeMoreWalls and rearrangeWalls failure yet additional walls remaining, dump them onto
the shortest path. This ensures all walls are spent even when no chokepoints exists, and may create
new chokepoints. Placing a wall is always safe because it never lies on a chokepoint so cannot
disconnect, and placing walls without disconnecting cannot decrease score.

Better performance than Scramble on Adversarial; about the same on typical maps as Overfill.

## Benchmarks and profiles

<!-- For consistency, whole benchmark suite must come from same run, so updates should replace
everything. This doesn't apply to profiles unless drastic changes to environment happen. -->

**Mean throughput (1 thread, 1 second, 3 samples)**

| Solver     | small1 | medium1 | large1 | huge1 |
| ---------- | ------ | ------- | ------ | ----- |
| Climb      | ~3500  | ~760    | ~120   | ~4    |
| Identity   | ~4400  | ~1450   | ~180   | ~10   |
| Walk       | ~1100  | ~170    | ~35    | ~1    |
| Chokepoint | ~13100 | ~4700   | ~850   | ~51   |
| Uncover    | ~9800  | ~3700   | ~790   | ~65   |
| Intersect  | ~11600 | ~4250   | ~1000  | ~75   |
| Ruin       | ~19000 | ~5300   | ~1180  | ~86   |
| Overfill   | ~35000 | ~13800  | ~4180  | ~625  |
| Scramble   | ~31300 | ~10800  | ~3680  | ~587  |
| Frontier   | ~33300 | ~13450  | ~4110  | ~645  |
| Dump       | ~34400 | ~11300  | ~3680  | ~537  |

**Median score (1 thread, 300 milliseconds, 10 samples)**

| Solver         | small1 | medium1 | large1 | huge1 |
| -------------- | ------ | ------- | ------ | ----- |
| Climb          | =43    | ~84     | ~170   | ~268  |
| Identity       | =43    | =84     | ~172   | ~339  |
| Walk (1000 ms) | =43    | =84     | ~181   | ~424  |
| Chokepoint     | =43    | =84     | ~173   | ~374  |
| Uncover        | =43    | =84     | ~178   | ~392  |
| Intersect      | =43    | =84     | ~178   | ~386  |
| Ruin           | =43    | ~84     | ~185   | ~410  |
| Overfill       | =43    | ~84     | =185   | ~436  |
| Scramble       | =43    | =84     | ~185   | ~436  |
| Frontier       | =43    | =84     | ~185   | ~421  |
| Dump           | =43    | =84     | ~185   | ~443  |

**Profile (async-profiler, 3 seconds CPU mode)**

| Solver     | huge1                                                         |
| ---------- | ------------------------------------------------------------- |
| Climb      | ~99% rearrangeWalls                                           |
| Identity   | ~98% rearrangeWalls, ~1% getCellsOnShortestPath               |
| Walk       | ~99% rearrangeWalls                                           |
| Chokepoint | ~95% rearrangeWalls, ~4% getChokepoints                       |
| Uncover    | ~85% rearrangeWalls, ~15% getChokepoints                      |
| Intersect  | ~96% rearrangeWalls, ~4% getChokepoints                       |
| Ruin       | ~97% rearrangeWalls, ~3% getChokepoints                       |
| Overfill   | ~71% rearrangeWalls, ~17% getChokepoints, ~11% placeMoreWalls |
| Scramble   | ~80% rearrangeWalls, ~14% getChokepoints, ~6% other           |
| Frontier   | ~66% rearrangeWalls, ~21% getChokepoints, ~10% placeMoreWalls |
| Dump       | ~73% rearrangeWalls, ~15% getChokepoints, ~9% placeMoreWalls  |
