# Future work

<!-- NOTE TO AGENT: This file is for humans. It is not relevant to you unless explicitly mentioned. -->

## Strategy speculation

Hill climbing should be done by moving blocks onto the current path. Without teleports, that's only possible way to improve a score. Everything else is strictly worse or wandering around on a plateu, which could be useful but probably a waste of time.

Searches can run a lot faster if its results are cached. A local movement of player walls often only affects local pathfinding. You can detect this by checking against the set of visited cells. If you know the snake must travel between two points, you can be even more specific.

The damage done by removing a given player wall for a route is equal to the difference between the cells it blocks according to Snake::distances. Some strategy needs to exist that can efficiently calculate the impact of adding a player wall as a change in distance rather than a whole re-computation.

Heuristic: assign cell values by making random throws, evaluating, and setting the value of a cell proportional to total path length in the maps where it was chosen.

Heuristic: solve a single route, then layer them. Vulnerable to blocking itself off.

Heuristic: solve a series of sub-graphs, then connect them. Allocate resources between sub-graphs. Better to understand sub-graphs as regions that might not be square? Then "Grid" data structure is wrong.

Represent the state space not as a set of grids to fill, but as a set of paths between nodes to block off. This may struggle with creating features to nowhere.

Genetic algorithm should use sets of connected player walls as features, rather than draw from its parents randomly. This is because cells are only useful when they are groups.

## Support

CRITICAL ERROR: Pathfinder fails on "Among shortest paths, the Snake prefers to go up, then right, then down, then left."
