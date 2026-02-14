# Theory

Pathery takes place on a planar connected unweighted undirected graph $G$ with vertices
$V_G$, edges $E_G$, checkpoints $C_G$, and teleports. Teleports are dark magic and will
not be discussed further. Since $G$ is derived from a rectangular grid, it has low
maximum degree, so $V_G \approx E_G$.

The player is presented with $N$ empty cells and has a supply $S$ of walls, where $S 
\leq N$. The existence of a system wall on a vertex permanently eliminates it from the
graph. The remaining vertices are either blocked (has a player wall) or open (does not
have a player wall). A vertex is empty iff it is open and does not contain other
features.

Every way to assign walls to at most $S$ vertices of $N$ total is a candidate solution.
Since we are looking for the best candidate solution, the problem is a state-space
search over candidate solutions. The number of states is:

$$
W = \sum_{i=0}^{S} {N \choose i}
$$

There usually aren't many interesting features, so $N \approx V$. In the limit where $S 
\approx N$, almost every vertex can be either blocked or open, so:

$$W \in O(2^V)$$

The problem may be infeasible to brute force for long, but that doesn't mean it's not
worth trying.

```pseudocode
forever {
	guess randomly;
}
```

Brute force is effective on small maps with limited walls. The Simple example has
$N = 60$ and $S = 7$, so $W = 442255978$. My machine can loop about 140k times per
second per thread, so if all candidate solutions are unique, it can exhaust the problem
in a handful of minutes.

Brute force is inefficient because there are many states and the majority are
low-scoring: there are many ways to wholly block the snake or let it get to its
destination with a few poorly-placed walls, and the rest can be set in a myriad of ways.

To score highly, we need to exploit the state space's structure. In many cases, adding,
removing, or moving a wall produces a small change in score; similar states produce
similar scores. Call these similar states neighbors.

From an initial state, exploring neighbors randomly is no better than guessing randomly
because we incorporate no information about our surroundings. If we can get from an
initial state to a high scoring state by exploring a minimum of $T$ steps, and each
state has $I$ successors, we explore $I^T$ states. But if exploring only the best state
with best-first search leads to a high scoring state, we explore only $I*T$ states.

```pseudocode
forever {
    seed an initial configuration;
    make it better until you can't anymore;
}
```
