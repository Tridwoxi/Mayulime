package think.domain.model;

import java.util.HashSet;

/**
    Pathery puzzle metadata and immutable initial grid.
 */
public final class Puzzle {

    private final String name;
    private final Feature[] features;
    private final int numRows;
    private final int numCols;
    private final int[] checkpoints;
    private final int blockingBudget;

    public Puzzle(
        final String name,
        final Feature[] features,
        final int numRows,
        final int numCols,
        final int[] checkpoints,
        final int blockingBudget
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
}
