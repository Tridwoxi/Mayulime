# Common: shared solver utilities and orphans

A class belongs here if it is used across multiple solver families or if it was eagerly written
before it was needed and so has nowhere to go.

The classes defined here (and the `ints` package) often take up a majority of program runtime and are widely relied upon. Performance is critical.

StandardEvaluator currently uses these optimizations:

- 1D array indexing
- loop unrolling
- boolean operation ordering
- caching

It has tried and rejected these optimizations:

- generational counters
- bidirectional BFS
- neighbor precomputation in arrays

It has not yet attempted these optimizations:

- bit-parallel BFS (may require batch evaluation)
- neighbor precomputation in object fields (may be incompatible with domain model)

Additional optimizations are still desired. The other classes are less optimized and should be
fixed up at some point.
