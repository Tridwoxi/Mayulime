package think.solvers.climbing;

import think.common.IntArrays;
import think.common.StandardEvaluator;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.solvers.Solver;

/**
    Basic random restart hill climber. The tactics used here should be common to all hill climbers:

    <ul>
        <li>Seed the board with a varying number of walls.
        <li>Improve score by placing or moving a single wall.
    </ul>

    @implNote
        This class is final and future versions should just copy and paste the whole thing because
        they are expected to evolve independently. This class should be deleted once it is
        sufficiently dominated by its successors.
 */
public final class ClimbV1Solver extends Solver {

    private final int[] initiallyBlankCells;

    public ClimbV1Solver(final ProposedSolution listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.initiallyBlankCells = getCellsWhere(puzzle.getFeatures(), Feature.BLANK);
    }

    @Override
    protected void solve() throws KilledException {
        for (;;) {
            checkAlive();
            propose(hillClimb());
        }
    }

    private Feature[] hillClimb() throws KilledException {
        final Feature[] features = getPuzzle().getFeatures();
        final int[] budgetBox = new int[] { getPuzzle().getBlockingBudget() - seed(features) };
        final int[] scoreBox = new int[] { StandardEvaluator.evaluate(getPuzzle(), features) };

        for (;;) {
            final int[] blankCells = getCellsWhere(features, Feature.BLANK);
            final int[] playerCells = getCellsWhere(features, Feature.PLAYER_WALL);
            IntArrays.shuffleInPlace(blankCells);
            IntArrays.shuffleInPlace(playerCells);
            if (placeMoreWalls(features, blankCells, budgetBox, scoreBox)) {
                continue;
            }
            if (rearrangeWalls(features, blankCells, playerCells, scoreBox)) {
                continue;
            }
            break;
        }
        return features;
    }

    private int seed(final Feature[] features) {
        IntArrays.shuffleInPlace(initiallyBlankCells);
        final int budget = getPuzzle().getBlockingBudget();
        for (int placement = 0; placement < budget; placement += 1) {
            features[initiallyBlankCells[placement]] = Feature.PLAYER_WALL;
            if (StandardEvaluator.evaluate(getPuzzle(), features) < 0) {
                features[initiallyBlankCells[placement]] = Feature.BLANK;
                return placement;
            }
        }
        return budget;
    }

    private boolean placeMoreWalls(
        final Feature[] features,
        final int[] blankCells,
        final int[] budgetBox,
        final int[] scoreBox
    ) {
        if (budgetBox[0] <= 0) {
            return false;
        }
        for (final int blankCell : blankCells) {
            features[blankCell] = Feature.PLAYER_WALL;
            final int newScore = StandardEvaluator.evaluate(getPuzzle(), features);
            if (newScore > scoreBox[0]) {
                scoreBox[0] = newScore;
                budgetBox[0] -= 1;
                return true;
            }
            features[blankCell] = Feature.BLANK;
        }
        return false;
    }

    private boolean rearrangeWalls(
        final Feature[] features,
        final int[] blankCells,
        final int[] playerCells,
        final int[] scoreBox
    ) {
        for (int blankIndex = 0; blankIndex < blankCells.length; blankIndex += 1) {
            final int blankCell = blankCells[blankIndex];
            for (int playerIndex = 0; playerIndex < playerCells.length; playerIndex += 1) {
                final int playerCell = playerCells[playerIndex];
                features[blankCell] = Feature.PLAYER_WALL;
                features[playerCell] = Feature.BLANK;
                blankCells[blankIndex] = playerCell;
                playerCells[playerIndex] = blankCell;
                final int newScore = StandardEvaluator.evaluate(getPuzzle(), features);
                if (newScore > scoreBox[0]) {
                    scoreBox[0] = newScore;
                    return true;
                }
                features[blankCell] = Feature.BLANK;
                features[playerCell] = Feature.PLAYER_WALL;
                blankCells[blankIndex] = blankCell;
                playerCells[playerIndex] = playerCell;
            }
        }
        return false;
    }

    private static int[] getCellsWhere(final Feature[] features, final Feature feature) {
        return IntArrays.ofRangeWhere(0, features.length, index -> features[index] == feature);
    }
}
