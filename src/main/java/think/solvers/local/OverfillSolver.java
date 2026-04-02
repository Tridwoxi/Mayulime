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

public final class OverfillSolver extends Solver {

    private final Random random;
    private final StandardEvaluator evaluator;
    private final DistanceFinder distances;
    private final int[] initiallyBlankCells;
    private final int[] checkpoints;
    private final int numRows;
    private final int numCols;
    private final int[] neighborBuffer;

    public OverfillSolver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.random = new Random();
        this.evaluator = new StandardEvaluator(puzzle);
        this.distances = new DistanceFinder(puzzle);
        this.initiallyBlankCells = getCellsWhere(puzzle.getFeatures(), Feature.BLANK);
        this.checkpoints = puzzle.getCheckpoints();
        this.numRows = puzzle.getNumRows();
        this.numCols = puzzle.getNumCols();
        this.neighborBuffer = new int[4];
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
        final int[] candidateScores = new int[playerCells.length];

        for (final int preRemovalChokepoint : preRemovalChokepoints) {
            checkAlive();
            features[preRemovalChokepoint] = Feature.PLAYER_WALL;
            Arrays.fill(candidateScores, 0);

            int[] fromCurrent = distances.find(features, checkpoints[0]);
            for (int segment = 0; segment < checkpoints.length - 1; segment += 1) {
                final int[] fromNext = distances.find(features, checkpoints[segment + 1]);
                final int blockedDistance = fromCurrent[checkpoints[segment + 1]];

                for (int playerIndex = 0; playerIndex < playerCells.length; playerIndex += 1) {
                    checkAlive();
                    if (candidateScores[playerIndex] == StandardEvaluator.NO_PATH_EXISTS) {
                        continue;
                    }
                    final int restoredDistance = getDistanceWithCellOpened(
                        playerCells[playerIndex],
                        fromCurrent,
                        fromNext,
                        blockedDistance
                    );
                    if (restoredDistance == DistanceFinder.UNREACHABLE) {
                        candidateScores[playerIndex] = StandardEvaluator.NO_PATH_EXISTS;
                        continue;
                    }
                    candidateScores[playerIndex] += restoredDistance;
                }
                fromCurrent = fromNext;
            }

            for (int playerIndex = 0; playerIndex < playerCells.length; playerIndex += 1) {
                checkAlive();
                if (candidateScores[playerIndex] <= scoreBox[0]) {
                    continue;
                }
                scoreBox[0] = candidateScores[playerIndex];
                features[playerCells[playerIndex]] = Feature.BLANK;
                return true;
            }

            features[preRemovalChokepoint] = Feature.BLANK;
        }
        return false;
    }

    private int getDistanceWithCellOpened(
        final int playerCell,
        final int[] fromStart,
        final int[] fromFinish,
        final int blockedDistance
    ) {
        final int throughOpenedCell = getDistanceThroughOpenedCell(
            playerCell,
            fromStart,
            fromFinish
        );
        if (blockedDistance == DistanceFinder.UNREACHABLE) {
            return throughOpenedCell;
        }
        if (throughOpenedCell == DistanceFinder.UNREACHABLE) {
            return blockedDistance;
        }
        return Math.min(blockedDistance, throughOpenedCell);
    }

    /**
        Any improvement after unblocking one wall must pass through that cell, so evaluating every
        ordered pair of distinct neighbors gives its exact contribution.
     */
    private int getDistanceThroughOpenedCell(
        final int playerCell,
        final int[] fromStart,
        final int[] fromFinish
    ) {
        int neighborCount = 0;
        final int row = playerCell / numCols;
        final int col = playerCell % numCols;
        if (row > 0) {
            neighborBuffer[neighborCount] = playerCell - numCols;
            neighborCount += 1;
        }
        if (col < numCols - 1) {
            neighborBuffer[neighborCount] = playerCell + 1;
            neighborCount += 1;
        }
        if (row < numRows - 1) {
            neighborBuffer[neighborCount] = playerCell + numCols;
            neighborCount += 1;
        }
        if (col > 0) {
            neighborBuffer[neighborCount] = playerCell - 1;
            neighborCount += 1;
        }

        int bestDistance = DistanceFinder.UNREACHABLE;
        for (int enterIndex = 0; enterIndex < neighborCount; enterIndex += 1) {
            final int enterCell = neighborBuffer[enterIndex];
            final int startDistance = fromStart[enterCell];
            if (startDistance == DistanceFinder.UNREACHABLE) {
                continue;
            }
            for (int exitIndex = 0; exitIndex < neighborCount; exitIndex += 1) {
                if (exitIndex == enterIndex) {
                    continue;
                }
                final int exitCell = neighborBuffer[exitIndex];
                final int finishDistance = fromFinish[exitCell];
                if (finishDistance == DistanceFinder.UNREACHABLE) {
                    continue;
                }
                final int candidateDistance = startDistance + finishDistance + 2;
                if (
                    bestDistance == DistanceFinder.UNREACHABLE ||
                    candidateDistance < bestDistance
                ) {
                    bestDistance = candidateDistance;
                }
            }
        }
        return bestDistance;
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
