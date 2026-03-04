package domain.model;

import java.util.EnumSet;
import java.util.HashSet;

/**
    Pathery puzzle metadata. The {@link Maze} itself is accessible as a field.
 */
public final class Puzzle {

    /**
        Assume all puzzles contain blank cells, system walls, player walls, and the unique
        checkpoint sequence. What additional mechanics do they have?
     */
    public enum Mechanic {
        TELEPORTS,
        MULTI_START, // And multi-finish.
        ICE_TILES,
    }

    private final String name;
    private final Maze maze;
    private final int[] checkpoints;
    private final int blockingBudget;
    private final EnumSet<Mechanic> mechanics;

    public Puzzle(
        final String name,
        final Maze maze,
        final int[] checkpoints,
        final int blockingBudget,
        final EnumSet<Mechanic> mechanics
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
        this.mechanics = EnumSet.copyOf(mechanics);
    }

    public String getName() {
        return name;
    }

    public Maze getMaze() {
        return maze;
    }

    public int[] getCheckpoints() {
        return checkpoints.clone();
    }

    public int getBlockingBudget() {
        return blockingBudget;
    }

    public EnumSet<Mechanic> getMechanics() {
        return EnumSet.copyOf(mechanics);
    }
}
