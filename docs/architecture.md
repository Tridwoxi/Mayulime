# Architecture

## Package structure

`docs/`: Supplementary documents as markdown.

`examples/`: Pathery problems the project is known to support as MapCodes.

`src/infra/io`: GUI, MapCode parsing, and logging.

`src/infra/main`: Program entry point. App is the normal entry point; Headless is a development-only alternative. Starts up the Manager. Connects backend to frontend.

`src/infra/tests`: Unit tests.

`src/think/`: Backend. The Manager is responsible for concurrency and worker
management. It creates the workers and recieves information with non-blocking callbacks.

`src/think/ana`: Static tools. Objects belong here when they are project-specific, such
as pathfinding and distance evaluation, but not quite strategy-specific.

`src/think/repr`: Data model. The Problem contains metadata, such as the order of
checkpoints and the player's wall supply, and the Pathery maze itself. This maze is
represented as a rectangular Grid of Features. Feature is an enum of things that appear
in the maze, such as a player wall. Classes defined here are instantiable.

`src/think/stra`: A Strategy is a runnable worker that uses the other think packages to
come up with better solutions to notify the Manager about.

`src/think/tools`: More static tools, such as Iteration and Structures, that are
not project-specific and do not import other project packages.

## Conventions

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

Commits use [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/)
style with required scope.
