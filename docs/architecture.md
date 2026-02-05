# Architecture

The project is a JavaFX Application.

## Package structure

`docs/`: Project description as markdown.

`examples/`: Pathery problems the project is known to support as MapCodes.

`src/app/`: Contains main class, tests, and GUI. Things here are relevant to entire
project or user experience.

`src/think/`: Backend. The Manager is responsible for concurrency and worker management.

`src/think/ana`: Static analysis tools. Subroutines belong here when they are
project-specific, such as pathfinding and distance evaluation.

`src/think/repr`: Data model. Classes defined here are instantiable.

`src/think/stra`: A Strategy is a runnable worker that uses the other think packages to
come up with better solutions to notify the Manager about.

`src/think/tools`: More static subroutines, such as Iteration and Structures, that are
not project-specific and do not import other project packages.

## Data model and flow

A Pathery MapCode is uploaded by the user in the GUI and sent to the app. The App
creates a Problem from the MapCode and sends it to the Manager, which sends it to
Strategies.

Data is sent from the App down the chain using method calls on instances. Data is sent
up the chain to the App using a dependecy-injected method reference, which is why Gui,
Manager, and Strategy acccept functional interfaces in their constructors.

When a Strategy attempts to solve a Problem, it makes calls to static methods in the
analysis and tools packages, and instantiates more objects from the representation
package.

The Problem contains metadata, such as the order of checkpoints and the player's wall
supply, and the Pathery map itself. This map is represented as a rectangular Grid of
Features. Feature is an enum of things that appear on the map, such as a player wall.

Solutions to the Problem are Grids of Features resembling the original map, but with
assignments of player wall features where there was previous empty cells.
