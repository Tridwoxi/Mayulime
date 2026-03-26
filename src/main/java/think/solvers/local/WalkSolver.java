package think.solvers.local;

import java.util.function.Consumer;
import think.common.IntArrays;
import think.common.StandardEvaluator;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.manager.Proposal;
import think.solvers.Solver;

public final class WalkSolver extends Solver {

    private static final int WALK_CYCLES = 3;
    private final StandardEvaluator evaluator;
    private final int[] initiallyBlankCells;

    public WalkSolver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.evaluator = new StandardEvaluator(puzzle);
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
        final int[] scoreBox = new int[] { evaluator.evaluate(features) };

        outer: for (;;) {
            checkAlive();
            final int[] blankCells = getCellsWhere(features, Feature.BLANK);
            final int[] playerCells = getCellsWhere(features, Feature.PLAYER_WALL);
            IntArrays.shuffleInPlace(blankCells);
            IntArrays.shuffleInPlace(playerCells);
            if (placeMoreWalls(features, blankCells, budgetBox, scoreBox)) {
                continue;
            }
            for (int attempt = 0; attempt < WALK_CYCLES; attempt += 1) {
                if (rearrangeWalls(features, blankCells, playerCells, scoreBox, true)) {
                    continue outer;
                }
            }
            if (rearrangeWalls(features, blankCells, playerCells, scoreBox, false)) {
                continue;
            }
            break;
        }
        return features;
    }

    private int seed(final Feature[] features) throws KilledException {
        IntArrays.shuffleInPlace(initiallyBlankCells);
        final int budget = getPuzzle().getBlockingBudget();
        for (int placement = 0; placement < budget; placement += 1) {
            checkAlive();
            features[initiallyBlankCells[placement]] = Feature.PLAYER_WALL;
            if (evaluator.evaluate(features) < 0) {
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
    ) throws KilledException {
        if (budgetBox[0] <= 0) {
            return false;
        }
        for (final int blankCell : blankCells) {
            checkAlive();
            features[blankCell] = Feature.PLAYER_WALL;
            final int newScore = evaluator.evaluate(features);
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
        final int[] scoreBox,
        final boolean isWalkAcceptable
    ) throws KilledException {
        for (int blankIndex = 0; blankIndex < blankCells.length; blankIndex += 1) {
            final int blankCell = blankCells[blankIndex];
            for (int playerIndex = 0; playerIndex < playerCells.length; playerIndex += 1) {
                checkAlive();
                final int playerCell = playerCells[playerIndex];
                features[blankCell] = Feature.PLAYER_WALL;
                features[playerCell] = Feature.BLANK;
                blankCells[blankIndex] = playerCell;
                playerCells[playerIndex] = blankCell;
                final int newScore = evaluator.evaluate(features);
                if (newScore > scoreBox[0]) {
                    scoreBox[0] = newScore;
                    return true;
                }
                if (isWalkAcceptable && newScore == scoreBox[0]) {
                    continue;
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
