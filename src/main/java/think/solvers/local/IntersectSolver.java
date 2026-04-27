package think.solvers.local;

import java.util.Arrays;
import java.util.function.BiConsumer;
import think.common.DistanceFinder;
import think.common.StandardEvaluator;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.ints.IntArrays;
import think.solvers.Solver;

public final class IntersectSolver extends Solver {

    private final StandardEvaluator evaluator;
    private final DistanceFinder distances;
    private final int[] initiallyBlankCells;
    private final int[] waypoints;

    public IntersectSolver(final BiConsumer<String, Tile[]> listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.evaluator = new StandardEvaluator(puzzle);
        this.distances = new DistanceFinder(puzzle);
        this.initiallyBlankCells = getCellsWhere(puzzle.tiles(), Tile.BLANK);
        this.waypoints = puzzle.waypoints();
    }

    @Override
    protected void solve() throws KilledException {
        for (;;) {
            checkAlive();
            propose(hillClimb());
        }
    }

    private Tile[] hillClimb() throws KilledException {
        final Tile[] state = getPuzzle().tiles();
        final int[] budgetBox = new int[] { getPuzzle().blockingBudget() - seed(state) };
        final int[] scoreBox = new int[] { evaluator.evaluate(state) };

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
        return state;
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
        final boolean[] isPreRemovalChokepoint = new boolean[state.length];
        for (final int preRemovalChokepoint : preRemovalChokepoints) {
            isPreRemovalChokepoint[preRemovalChokepoint] = true;
        }
        for (final int playerCell : playerCells) {
            checkAlive();
            state[playerCell] = Tile.BLANK;
            final int[] postRemovalChokepoints = getChokepoints(state);
            IntArrays.shuffleInPlace(postRemovalChokepoints);
            for (final int postRemovalChokepoint : postRemovalChokepoints) {
                checkAlive();
                if (!isPreRemovalChokepoint[postRemovalChokepoint]) {
                    continue;
                }
                state[postRemovalChokepoint] = Tile.PLAYER_WALL;
                final int newScore = evaluator.evaluate(state);
                if (newScore > scoreBox[0]) {
                    scoreBox[0] = newScore;
                    return true;
                }
                state[postRemovalChokepoint] = Tile.BLANK;
            }
            state[playerCell] = Tile.PLAYER_WALL;
        }
        return false;
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
