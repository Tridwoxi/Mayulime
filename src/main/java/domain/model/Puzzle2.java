package domain.model;

import java.util.HashSet;

/**
    Pathery problem specification metadata. The maze itself is accessible as a field.
 */
public final class Puzzle2 {

    private final String name;
    private final Maze maze;
    private final int[] checkpoints;
    private final int blockingBudget;

    public Puzzle2(
        final String name,
        final Maze maze,
        final int[] checkpoints,
        final int blockingBudget
    ) {
        final HashSet<Integer> seen = new HashSet<>(checkpoints.length);
        for (final int checkpoint : checkpoints) {
            if (!maze.isInBounds(checkpoint) || !seen.add(checkpoint)) {
                throw new IllegalArgumentException();
            }
        }
        this.name = name;
        this.maze = maze;
        this.checkpoints = checkpoints.clone();
        this.blockingBudget = blockingBudget;
    }

    public Maze getMaze() {
        return maze;
    }

    public String getName() {
        return name;
    }

    public int[] getCheckpoints() {
        return checkpoints.clone();
    }

    public int getBlockingBudget() {
        return blockingBudget;
    }
}
