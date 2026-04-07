package think.solvers.local;

import java.util.function.Consumer;
import think.common.DistanceFinder;
import think.common.StandardEvaluator;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.ints.IntArrays;
import think.manager.Proposal;
import think.solvers.Solver;

public final class IdentitySolver extends Solver {

    private final StandardEvaluator evaluator;
    private final DistanceFinder distances;
    private final int[] initiallyBlankCells;
    private final int[] checkpoints;

    public IdentitySolver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.evaluator = new StandardEvaluator(puzzle);
        this.distances = new DistanceFinder(puzzle);
        this.initiallyBlankCells = getCellsWhere(puzzle.getFeatures(), Feature.BLANK);
        this.checkpoints = puzzle.getCheckpoints();
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

        for (;;) {
            checkAlive();
            final int[] blankCells = getCellsOnShortestPath(features);
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
        final int[] scoreBox
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
                features[blankCell] = Feature.BLANK;
                features[playerCell] = Feature.PLAYER_WALL;
                blankCells[blankIndex] = blankCell;
                playerCells[playerIndex] = playerCell;
            }
        }
        return false;
    }

    private int[] getCellsOnShortestPath(final Feature[] features) {
        final boolean[] onPath = new boolean[features.length];
        int[] fromCurrent = distances.find(features, checkpoints[0]);
        for (int segment = 0; segment < checkpoints.length - 1; segment += 1) {
            final int[] fromNext = distances.find(features, checkpoints[segment + 1]);
            final int totalDistance = fromCurrent[checkpoints[segment + 1]];
            if (totalDistance == DistanceFinder.UNREACHABLE) {
                return IntArrays.EMPTY;
            }
            for (int cell = 0; cell < features.length; cell += 1) {
                if (
                    fromCurrent[cell] != DistanceFinder.UNREACHABLE &&
                    fromNext[cell] != DistanceFinder.UNREACHABLE &&
                    fromCurrent[cell] + fromNext[cell] == totalDistance
                ) {
                    onPath[cell] = true;
                }
            }
            fromCurrent = fromNext;
        }
        return IntArrays.filteredCopy(getCellsWhere(features, Feature.BLANK), cell -> onPath[cell]);
    }

    private static int[] getCellsWhere(final Feature[] features, final Feature feature) {
        return IntArrays.ofRangeWhere(0, features.length, index -> features[index] == feature);
    }
}
