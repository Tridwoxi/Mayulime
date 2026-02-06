# Theory

Pathery takes place on a planar connected unweighted undirected graph $G$ with vertices
$V$, edges $E$, checkpoints $C$, and teleports. Teleports are dark magic and will not
be discussed further. Since $G$ is derived from a rectangular grid, it has low maximum
degree, so $V \approx E$.

The player is presented with $N$ empty cells and has a supply $S$ of walls, where $S 
\leq N$. The existence of a system wall on a vertex permanently eliminates it from the
graph. The remaining vertices are either blocked (has a player wall) or open (does not
have a player wall). A vertex is empty iff it is open and does not contain other
features.

## Strategies

### Guess randomly

The number of ways $W$ to select up to $S$ vertices from $N$ total is:

$$
W=\sum_{i=0}^{S}{N \choose i}
$$

There usually aren't many interesting features, so $N \approx V$. In the limit where $S 
\approx N$, almost every vertex can be either blocked or open, so:

$$W \in O(2^V)$$

The problem may be infeasible to brute force for long, but that doesn't mean it's not
worth trying. `RandomGuesser` uses the following approach:

```pseudocode
forever {
	guess randomly;
	evaluate;
}
```

On my machine, it can loop about 140k times per second per thread on a Simple with $N = 
60$ and $S = 7$. If guessing randomly never repeats, it can brute force the problem in
a handful of minutes on 10 threads.
