# Common: shared solver utilities and orphans

A class belongs here if it is used across multiple solver families or if it was eagerly written
before it was needed and so has nowhere to go.

Performance (in constant factors) is the name of the game here. StandardEvaluator and IntDeque are
heavily optimized in both time and memory. The current lineup is 1D array indexing, loop unrolling,
boolean operation ordering, bitwise operations, and caching. Tried and rejected are generational
counters, bidirectional BFS, and neighbor precomputation in arrays. Bit-parallel BFS may work but
requires batch evaluation. Neighbor precomputation in object fields may work but is incompatible
with the domain model. Other classes are optimized to varying degrees.
