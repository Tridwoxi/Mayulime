# Exact solvers

This package contains solvers that provably explore the entire solution space.

## Benchmarks and profiles

**Milliseconds to complete (1 trial)**

| Solver    | small1  | small2 | small3 |
| --------- | ------- | ------ | ------ |
| Enumerate | ~106000 | ~10800 | ~84900 |

**Profile (async-profiler, 3 seconds CPU mode)**

| Solver    | small1                         |
| --------- | ------------------------------ |
| Enumerate | ~96% evaluate, ~3% solve outer |
