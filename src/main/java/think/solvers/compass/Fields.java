package think.solvers.compass;

import think.common.DistanceFinder;
import think.domain.model.Puzzle;
import think.domain.model.Tile;

class Fields {

    private final DistanceFinder finder;
    private final int puzzleSize;
    private final int[] waypoints;
    private final int[][] fieldsFromSources;
    private final int[][] fieldsBetweenSources;

    Fields(final Puzzle puzzle) {
        this.puzzleSize = puzzle.getNumRows() * puzzle.getNumCols();
        this.finder = new DistanceFinder(puzzle);
        this.waypoints = puzzle.getWaypoints();
        this.fieldsBetweenSources = new int[waypoints.length][puzzleSize];
        this.fieldsFromSources = new int[waypoints.length - 1][puzzleSize];
    }

    void adapt(final Tile[] state) {
        for (int index = 0; index < waypoints.length; index += 1) {
            finder.find(fieldsFromSources[index], state, waypoints[index]);
        }
        for (int waypointIndex = 0; waypointIndex < waypoints.length - 1; waypointIndex += 1) {
            final int[] startField = fieldsFromSources[waypointIndex];
            final int[] finishField = fieldsFromSources[waypointIndex + 1];
            final int[] betweenField = fieldsBetweenSources[waypointIndex];
            for (int tileIndex = 0; tileIndex < puzzleSize; tileIndex += 1) {
                betweenField[tileIndex] = startField[tileIndex] + finishField[tileIndex];
            }
        }
    }

    int[] fieldViewAt(final int waypointIndex) {
        return fieldsFromSources[waypointIndex];
    }

    int[] fieldViewAfter(final int waypointIndex) {
        return fieldsBetweenSources[waypointIndex];
    }
}
