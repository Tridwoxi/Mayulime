package think.solvers.random;

import think.common.IntArrays;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.solvers.Solver;

public final class RandomSolver extends Solver {

    private final int[] emptyCellIndices;
    private final RestrictedBinomial wallDistribution;

    public RandomSolver(final ProposedSolution listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.emptyCellIndices = getEmptyCellIndices(puzzle.getFeatures());
        this.wallDistribution = new RestrictedBinomial(
            emptyCellIndices.length,
            puzzle.getBlockingBudget()
        );
    }

    @Override
    protected void solve() throws KilledException {
        while (true) {
            checkAlive();
            propose(generateRandomSolution());
        }
    }

    private Feature[] generateRandomSolution() {
        final Feature[] grid = getPuzzle().getFeatures();
        IntArrays.shuffleInPlace(emptyCellIndices);

        final int numWalls = wallDistribution.sample();
        for (int placement = 0; placement < numWalls; placement += 1) {
            final int cell = emptyCellIndices[placement];
            grid[cell] = Feature.PLAYER_WALL;
        }
        return grid;
    }

    private static int[] getEmptyCellIndices(final Feature[] features) {
        final int[] emptyCellIndices = new int[features.length];
        for (int index = 0; index < features.length; index += 1) {
            emptyCellIndices[index] = features[index] == Feature.BLANK ? index : -1;
        }
        return IntArrays.filteredCopy(emptyCellIndices, -1);
    }
}
