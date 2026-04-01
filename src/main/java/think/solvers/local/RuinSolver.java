package think.solvers.local;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;
import think.common.DistanceFinder;
import think.common.IntArrays;
import think.common.StandardEvaluator;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.manager.Proposal;
import think.solvers.Solver;

public final class RuinSolver extends Solver {

    private final Random random;
    private final StandardEvaluator evaluator;
    private final DistanceFinder distances;
    private final int[] initiallyBlankCells;
    private final int[] checkpoints;

    public RuinSolver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.random = new Random();
        this.evaluator = new StandardEvaluator(puzzle);
        this.distances = new DistanceFinder(puzzle);
        this.initiallyBlankCells = getCellsWhere(puzzle.getFeatures(), Feature.BLANK);
        this.checkpoints = puzzle.getCheckpoints();
    }

    @Override
    protected void solve() throws KilledException {
        Feature[] best = hillClimbFromSeed();
        int bestScore = evaluator.evaluate(best);
        propose(best);

        for (;;) {
            checkAlive();
            final Feature[] ruined = ruin(best);
            final Feature[] candidate = hillClimbFromFeatures(ruined);
            final int candidateScore = evaluator.evaluate(candidate);
            propose(candidate);
            if (candidateScore >= bestScore) {
                best = candidate;
                bestScore = candidateScore;
            }
        }
    }

    private Feature[] hillClimbFromSeed() throws KilledException {
        final Feature[] features = getPuzzle().getFeatures();
        final int[] budgetBox = new int[] { getPuzzle().getBlockingBudget() - seed(features) };
        final int[] scoreBox = new int[] { evaluator.evaluate(features) };
        climbLoop(features, budgetBox, scoreBox);
        return features;
    }

    private Feature[] hillClimbFromFeatures(final Feature[] features) throws KilledException {
        final int wallCount = getCellsWhere(features, Feature.PLAYER_WALL).length;
        final int[] budgetBox = new int[] { getPuzzle().getBlockingBudget() - wallCount };
        final int[] scoreBox = new int[] { evaluator.evaluate(features) };
        climbLoop(features, budgetBox, scoreBox);
        return features;
    }

    private void climbLoop(final Feature[] features, final int[] budgetBox, final int[] scoreBox)
        throws KilledException {
        for (;;) {
            checkAlive();
            final int[] blankCells = getChokepoints(features);
            if (blankCells.length == 0) {
                break;
            }
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

    private Feature[] ruin(final Feature[] features) {
        final Feature[] ruined = features.clone();
        final int numRows = getPuzzle().getNumRows();
        final int numCols = getPuzzle().getNumCols();
        final int width = random.nextInt(1, numCols + 1);
        final int height = random.nextInt(1, numRows + 1);
        final int top = random.nextInt(numRows);
        final int left = random.nextInt(numCols);
        for (int dy = 0; dy < height; dy += 1) {
            final int row = (top + dy) % numRows;
            for (int dx = 0; dx < width; dx += 1) {
                final int col = (left + dx) % numCols;
                final int cell = row * numCols + col;
                if (ruined[cell] == Feature.PLAYER_WALL) {
                    ruined[cell] = Feature.BLANK;
                }
            }
        }
        return ruined;
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
        final int[] preRemovalChokepoints,
        final int[] playerCells,
        final int[] scoreBox
    ) throws KilledException {
        final boolean[] isPreRemovalChokepoint = new boolean[features.length];
        for (final int preRemovalChokepoint : preRemovalChokepoints) {
            isPreRemovalChokepoint[preRemovalChokepoint] = true;
        }
        for (final int playerCell : playerCells) {
            checkAlive();
            features[playerCell] = Feature.BLANK;
            final int[] postRemovalChokepoints = getChokepoints(features);
            IntArrays.shuffleInPlace(postRemovalChokepoints);
            for (final int postRemovalChokepoint : postRemovalChokepoints) {
                checkAlive();
                if (!isPreRemovalChokepoint[postRemovalChokepoint]) {
                    continue;
                }
                features[postRemovalChokepoint] = Feature.PLAYER_WALL;
                final int newScore = evaluator.evaluate(features);
                if (newScore > scoreBox[0]) {
                    scoreBox[0] = newScore;
                    return true;
                }
                features[postRemovalChokepoint] = Feature.BLANK;
            }
            features[playerCell] = Feature.PLAYER_WALL;
        }
        return false;
    }

    private int[] getChokepoints(final Feature[] features) {
        final boolean[] isChokepoint = new boolean[features.length];
        final int[] layerCount = new int[features.length];

        int[] fromCurrent = distances.find(features, checkpoints[0]);
        for (int segment = 0; segment < checkpoints.length - 1; segment += 1) {
            final int[] fromNext = distances.find(features, checkpoints[segment + 1]);
            final int totalDistance = fromCurrent[checkpoints[segment + 1]];
            if (totalDistance == DistanceFinder.UNREACHABLE) {
                return IntArrays.EMPTY;
            }
            Arrays.fill(layerCount, 0);
            for (int cell = 0; cell < features.length; cell += 1) {
                if (fromCurrent[cell] + fromNext[cell] == totalDistance) {
                    layerCount[fromCurrent[cell]] += 1;
                }
            }
            for (int cell = 0; cell < features.length; cell += 1) {
                if (
                    fromCurrent[cell] + fromNext[cell] == totalDistance &&
                    layerCount[fromCurrent[cell]] == 1
                ) {
                    isChokepoint[cell] = true;
                }
            }
            fromCurrent = fromNext;
        }
        return IntArrays.ofRangeWhere(
            0,
            features.length,
            cell -> isChokepoint[cell] && features[cell] == Feature.BLANK
        );
    }

    private static int[] getCellsWhere(final Feature[] features, final Feature feature) {
        return IntArrays.ofRangeWhere(0, features.length, index -> features[index] == feature);
    }
}
