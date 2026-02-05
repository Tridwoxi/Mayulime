# Style guide

Variable names are descriptive. Abbreviation is acceptable but discouraged. One letter
names are prohibited.

Currently, constant-factor performance improvements are not a priority. The comment
"PERF:" indicates areas for work if performance gains are necessary.

Null pointers may be produced and checked for, but `null` is never accepted or passed
by any subroutine defined in this project.

Streaming and functional programming are encouraged but not required. Use ArrayLists
instead of raw arrays.

Assertions are for both conditions and invariants because they are close to source.
Tests are written only when assertions are difficult to write. Assertions are permitted
to slow down the system by an arbritrary amount.

Extending or implementing custom classes and interfaces is discouraged but permitted up
to one layer deep.

Commits use conventional commits style with required scope.
