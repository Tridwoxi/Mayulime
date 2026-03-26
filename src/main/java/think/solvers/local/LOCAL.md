# Local search

This package is home to the local state space search solver family. Copy and paste solvers instead
of using inheritance because solvers need to evolve independently. Record experimental results.

## Solver lineage

**Climb**

Random restart hill climber. Seed the board with a varying number of walls. Improve score by
placing or moving a single wall.

Given the neighbor set "place one wall (subject to budget)" and "move one wall", all proposals are
locally optimal by brute force check.

Throughput ~2500 on small1, ~5 on huge1. Agreement ~0.08 on small1.

Instant on small, effective for medium to large.

**Identity builds on Climb**

If a vertex is not on a shortest path, blocking it cannot increase score. A vertex $v$ is on a
shortest path iff $d(s, v) + d(v, f) = d(s, f)$.

Throughput ~4900 on small1, ~10 on huge1. Profile is ~92% StandardEvaluator. Speedup comes from
using less of it.

**Walk builds on Climb**

Before the expensive O(n^3) exhaustive rearrange step, let's run the same thing many times but also
accept neutral moves.

Not promising. For 3x walk, throughput ~2500, agreement ~0.01 on small1. Throughput ~22 on huge1,
weirdly. I don't know why.
