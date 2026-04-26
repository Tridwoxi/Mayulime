# Plans

For benchmarks, maybe it's easier to just dump information into a file then analyze it with Python
instead of trying to analyze it in Java. Since half the time it goes to graph anyway...

---

Sufficient exploration has happened in the combinatorial familiy of solvers (in package `local`)
that a solver written for effectiveness instead of insight should be written. It should include:

- OverfillSolver-style 1-opt hill climbing loop
- Double rectangle ILS ("ruin and repair" in this repo)
- Candidate storage in MAP-Elites layer possibly governed by a bandit policy

---

Genetic algorithms have not yet been tried as solvers. They should be tried.

---

We need a search over collections-of-walls-space instead of wall-space. Perhaps something like a
web between system walls, or detecting min-cuts (BFS layer size detection already exists) and
dropping a barrier of walls in there. I've been thinking of submerging the maze in soap and seeing
where connections form between player walls.

---

Somehow, the solver needs to figure out that there are 4 interesting cells in Adversarial and they
can be grouped into 2 pairs that are separated.

---

Can StandardEvaluator run faster?

---

ClimbSolver-style seeding can happen in `O(bfs * log(budget))` since without teleports adding more
walls can only increase score or disconnect. Somehow this trick works for placeMoreWalls too (it
really shouldn't). Overfill rearrangeWalls can early terminate.

---

The graph usually looks like a tree in optimal solutions. There has to be information in here.
Under unlimited wall supply, it can be made to look like a tree if it is not yet one.

---

Nudging a single wall can cause walls on a cross from its new (or old?) position to become useless
and removeable. Walls surrounded on 3 sides are useless, but that is rare. More generally, a
mechanism to extract walls would be nice.

---

Solvers should be able to start from a partial result with already-placed walls.
