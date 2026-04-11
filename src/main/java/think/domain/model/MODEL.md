# Model: Puzzle and state representation

Representation is split between long-lived metadata in `Puzzle`, and states as `Tile[]`.

Solvers, particularly local search algorithms, tend to generate and modify many states but only
ever see one puzzle.

Semantically, values like waypoint order are properties of the puzzle, not of individual states,
so the puzzle stores them. The split also improves performance since metadata is rarely duplicated.

Earlier implementations tried a richer model with cell record-based indexing or sets. The cells
were probably fine, but the sets were terrible since the workload is performance-bottlenecked.
Arrays have good cache locality, no boxing, and low abstraction overhead.

Raw array support is worse than the Java Collections Framework, but the pain is eased with a tiny
utility class and you still need to track a lot of the same things regardless of representation.
Also, the earlier model was not easier to use in practice since you had to memorize an API when raw
arrays are already simple.

States are cloneable. Java's `Cloneable` may be a fundamentally flawed design since it's easy to
make mistakes with, but it's perfect once you memorize it works on raw arrays and never use it for
anything else.

Most correctness checking is done by the parser since it already knows so much about MapCodes. The
puzzle happens to do validation too, but it's probably a waste of effort. Correctness is validated
again later upon proposal to the manager because solvers can generate arbitrary states. Ideally,
we'd catch bad states when they are created, but the rich model described above has other failings.
