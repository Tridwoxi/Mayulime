package think.solvers.naive;

import java.util.function.Consumer;
import java.util.function.IntPredicate;
import think.common.IntArrays;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.manager.Proposal;
import think.solvers.Solver;

public final class RandomSolver extends Solver {

    private final int[] blankCellIndices;
    private final RestrictedBinomial wallDistribution;

    public RandomSolver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.blankCellIndices = getBlankCellIndices(puzzle.getFeatures());
        this.wallDistribution = new RestrictedBinomial(
            blankCellIndices.length,
            puzzle.getBlockingBudget()
        );
    }

    @Override
    protected void solve() throws KilledException {
        while (true) {
            checkAlive();
            propose(generateRandomProposal());
        }
    }

    private Feature[] generateRandomProposal() {
        final Feature[] maze = getPuzzle().getFeatures();
        IntArrays.shuffleInPlace(blankCellIndices);

        final int numWalls = wallDistribution.sample();
        for (int placement = 0; placement < numWalls; placement += 1) {
            final int cell = blankCellIndices[placement];
            maze[cell] = Feature.PLAYER_WALL;
        }
        return maze;
    }

    private static int[] getBlankCellIndices(final Feature[] features) {
        final IntPredicate isBlank = index -> features[index] == Feature.BLANK;
        return IntArrays.ofRangeWhere(0, features.length, isBlank);
    }
}
