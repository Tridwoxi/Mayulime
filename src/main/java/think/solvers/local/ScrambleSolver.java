package think.solvers.local;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;
import think.common.DistanceFinder;
import think.common.StandardEvaluator;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.ints.IntArrays;
import think.manager.Proposal;
import think.solvers.Solver;

public final class ScrambleSolver extends Solver {

    private final Random random;
    private final StandardEvaluator evaluator;
    private final DistanceFinder distances;
    private final int[] initiallyBlankCells;
    private final int[] waypoints;
    private final int numRows;
    private final int numCols;
    private final int[] neighborBuffer;
    private final int[] regionBuffer;

    public ScrambleSolver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.random = new Random();
        this.evaluator = new StandardEvaluator(puzzle);
        this.distances = new DistanceFinder(puzzle);
        this.initiallyBlankCells = getCellsWhere(puzzle.tiles(), Tile.BLANK);
        this.waypoints = puzzle.waypoints();
        this.numRows = puzzle.numRows();
        this.numCols = puzzle.numCols();
        this.neighborBuffer = new int[4];
        this.regionBuffer = new int[puzzle.tiles().length];
    }

    @Override
    protected void solve() throws KilledException {
        Tile[] best = hillClimbFromSeed();
        int bestScore = evaluator.evaluate(best);
        propose(best);

        for (;;) {
            checkAlive();
            final Tile[] scrambled = scramble(best);
            final Tile[] candidate = hillClimbFromState(scrambled);
            final int candidateScore = evaluator.evaluate(candidate);
            propose(candidate);
            if (candidateScore >= bestScore) {
                best = candidate;
                bestScore = candidateScore;
            }
        }
    }

    private Tile[] hillClimbFromSeed() throws KilledException {
        final Tile[] state = getPuzzle().tiles();
        final int[] budgetBox = new int[] { getPuzzle().blockingBudget() - seed(state) };
        final int[] scoreBox = new int[] { evaluator.evaluate(state) };
        climbLoop(state, budgetBox, scoreBox);
        return state;
    }

    private Tile[] hillClimbFromState(final Tile[] state) throws KilledException {
        final int wallCount = getCellsWhere(state, Tile.PLAYER_WALL).length;
        final int[] budgetBox = new int[] { getPuzzle().blockingBudget() - wallCount };
        final int[] scoreBox = new int[] { evaluator.evaluate(state) };
        climbLoop(state, budgetBox, scoreBox);
        return state;
    }

    private void climbLoop(final Tile[] state, final int[] budgetBox, final int[] scoreBox)
        throws KilledException {
        for (;;) {
            checkAlive();
            final int[] blankCells = getChokepoints(state);
            if (blankCells.length == 0) {
                break;
            }
            final int[] playerCells = getCellsWhere(state, Tile.PLAYER_WALL);
            IntArrays.shuffleInPlace(blankCells);
            IntArrays.shuffleInPlace(playerCells);
            if (placeMoreWalls(state, blankCells, budgetBox, scoreBox)) {
                continue;
            }
            if (rearrangeWalls(state, blankCells, playerCells, scoreBox)) {
                continue;
            }
            break;
        }
    }

    private int seed(final Tile[] state) throws KilledException {
        IntArrays.shuffleInPlace(initiallyBlankCells);
        final int budget = getPuzzle().blockingBudget();
        for (int placement = 0; placement < budget; placement += 1) {
            checkAlive();
            state[initiallyBlankCells[placement]] = Tile.PLAYER_WALL;
            if (evaluator.evaluate(state) < 0) {
                state[initiallyBlankCells[placement]] = Tile.BLANK;
                return placement;
            }
        }
        return budget;
    }

    private Tile[] scramble(final Tile[] state) throws KilledException {
        final Tile[] scrambled = state.clone();
        final int width = random.nextInt(1, numCols + 1);
        final int height = random.nextInt(1, numRows + 1);
        final int top = random.nextInt(numRows);
        final int left = random.nextInt(numCols);

        int regionCount = 0;
        int removedWalls = 0;
        for (int dy = 0; dy < height; dy += 1) {
            final int row = (top + dy) % numRows;
            for (int dx = 0; dx < width; dx += 1) {
                final int col = (left + dx) % numCols;
                final int cell = row * numCols + col;
                if (scrambled[cell] == Tile.PLAYER_WALL) {
                    scrambled[cell] = Tile.BLANK;
                    removedWalls += 1;
                }
                if (scrambled[cell] == Tile.BLANK) {
                    regionBuffer[regionCount] = cell;
                    regionCount += 1;
                }
            }
        }

        final int[] regionCells = Arrays.copyOf(regionBuffer, regionCount);
        IntArrays.shuffleInPlace(regionCells);
        final int localBudget = Math.min(removedWalls, regionCells.length);
        for (int placement = 0; placement < localBudget; placement += 1) {
            checkAlive();
            final int cell = regionCells[placement];
            scrambled[cell] = Tile.PLAYER_WALL;
            if (evaluator.evaluate(scrambled) < 0) {
                scrambled[cell] = Tile.BLANK;
                break;
            }
        }
        return scrambled;
    }

    private boolean placeMoreWalls(
        final Tile[] state,
        final int[] blankCells,
        final int[] budgetBox,
        final int[] scoreBox
    ) throws KilledException {
        if (budgetBox[0] <= 0) {
            return false;
        }
        for (final int blankCell : blankCells) {
            checkAlive();
            state[blankCell] = Tile.PLAYER_WALL;
            final int newScore = evaluator.evaluate(state);
            if (newScore > scoreBox[0]) {
                scoreBox[0] = newScore;
                budgetBox[0] -= 1;
                return true;
            }
            state[blankCell] = Tile.BLANK;
        }
        return false;
    }

    private boolean rearrangeWalls(
        final Tile[] state,
        final int[] preRemovalChokepoints,
        final int[] playerCells,
        final int[] scoreBox
    ) throws KilledException {
        final int[] candidateScores = new int[playerCells.length];

        for (final int preRemovalChokepoint : preRemovalChokepoints) {
            checkAlive();
            state[preRemovalChokepoint] = Tile.PLAYER_WALL;
            Arrays.fill(candidateScores, 0);

            int[] fromCurrent = distances.find(state, waypoints[0]);
            for (int segment = 0; segment < waypoints.length - 1; segment += 1) {
                final int[] fromNext = distances.find(state, waypoints[segment + 1]);
                final int blockedDistance = fromCurrent[waypoints[segment + 1]];

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
                state[playerCells[playerIndex]] = Tile.BLANK;
                return true;
            }

            state[preRemovalChokepoint] = Tile.BLANK;
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
                    bestDistance == DistanceFinder.UNREACHABLE || candidateDistance < bestDistance
                ) {
                    bestDistance = candidateDistance;
                }
            }
        }
        return bestDistance;
    }

    private int[] getChokepoints(final Tile[] state) {
        final boolean[] isChokepoint = new boolean[state.length];
        final int[] layerCount = new int[state.length];

        int[] fromCurrent = distances.find(state, waypoints[0]);
        for (int segment = 0; segment < waypoints.length - 1; segment += 1) {
            final int[] fromNext = distances.find(state, waypoints[segment + 1]);
            final int totalDistance = fromCurrent[waypoints[segment + 1]];
            if (totalDistance == DistanceFinder.UNREACHABLE) {
                return IntArrays.EMPTY;
            }
            Arrays.fill(layerCount, 0);
            for (int cell = 0; cell < state.length; cell += 1) {
                if (fromCurrent[cell] + fromNext[cell] == totalDistance) {
                    layerCount[fromCurrent[cell]] += 1;
                }
            }
            for (int cell = 0; cell < state.length; cell += 1) {
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
            state.length,
            cell -> isChokepoint[cell] && state[cell] == Tile.BLANK
        );
    }

    private static int[] getCellsWhere(final Tile[] state, final Tile tile) {
        return IntArrays.ofRangeWhere(0, state.length, index -> state[index] == tile);
    }
}
