package think.domain.model;

import java.util.EnumSet;
import java.util.HashSet;

/**
    Pathery puzzle metadata and immutable initial grid.
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
    private final Feature[] features;
    private final int numRows;
    private final int numCols;
    private final int[] checkpoints;
    private final int blockingBudget;
    private final EnumSet<Mechanic> mechanics;

    public Puzzle(
        final String name,
        final Feature[] features,
        final int numRows,
        final int numCols,
        final int[] checkpoints,
        final int blockingBudget,
        final EnumSet<Mechanic> mechanics
    ) {
        final HashSet<Integer> seen = new HashSet<>(checkpoints.length);
        for (final int checkpoint : checkpoints) {
            if (checkpoint < 0 || checkpoint >= numRows * numCols || !seen.add(checkpoint)) {
                throw new IllegalArgumentException();
            }
        }
        this.name = name;
        this.features = features.clone();
        this.numRows = numRows;
        this.numCols = numCols;
        this.checkpoints = checkpoints.clone();
        this.blockingBudget = blockingBudget;
        this.mechanics = EnumSet.copyOf(mechanics);
    }

    public String getName() {
        return name;
    }

    public Feature[] getFeatures() {
        return features.clone();
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
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
