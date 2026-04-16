package think.solvers.naive;

import java.util.function.Consumer;
import java.util.function.IntPredicate;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.ints.IntArrays;
import think.manager.Proposal;
import think.solvers.Solver;

public final class RandomSolver extends Solver {

    private final int[] blankCellIndices;
    private final RestrictedBinomial wallDistribution;

    public RandomSolver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.blankCellIndices = getBlankCellIndices(puzzle.tiles());
        this.wallDistribution = new RestrictedBinomial(
            blankCellIndices.length,
            puzzle.blockingBudget()
        );
    }

    @Override
    protected void solve() throws KilledException {
        while (true) {
            checkAlive();
            propose(generateRandomProposal());
        }
    }

    private Tile[] generateRandomProposal() {
        final Tile[] maze = getPuzzle().tiles();
        IntArrays.shuffleInPlace(blankCellIndices);

        final int numWalls = wallDistribution.sample();
        for (int placement = 0; placement < numWalls; placement += 1) {
            final int cell = blankCellIndices[placement];
            maze[cell] = Tile.PLAYER_WALL;
        }
        return maze;
    }

    private static int[] getBlankCellIndices(final Tile[] state) {
        final IntPredicate isBlank = index -> state[index] == Tile.BLANK;
        return IntArrays.ofRangeWhere(0, state.length, isBlank);
    }
}
