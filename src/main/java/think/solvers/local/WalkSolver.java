package think.solvers.local;

import java.util.function.BiConsumer;
import think.common.StandardEvaluator;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.ints.IntArrays;
import think.solvers.Solver;

public final class WalkSolver extends Solver {

    private static final int WALK_CYCLES = 3;
    private final StandardEvaluator evaluator;
    private final int[] initiallyBlankCells;

    public WalkSolver(final BiConsumer<String, Tile[]> listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.evaluator = new StandardEvaluator(puzzle);
        this.initiallyBlankCells = getCellsWhere(puzzle.tiles(), Tile.BLANK);
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

        outer: for (;;) {
            checkAlive();
            final int[] blankCells = getCellsWhere(state, Tile.BLANK);
            final int[] playerCells = getCellsWhere(state, Tile.PLAYER_WALL);
            IntArrays.shuffleInPlace(blankCells);
            IntArrays.shuffleInPlace(playerCells);
            if (placeMoreWalls(state, blankCells, budgetBox, scoreBox)) {
                continue;
            }
            for (int attempt = 0; attempt < WALK_CYCLES; attempt += 1) {
                if (rearrangeWalls(state, blankCells, playerCells, scoreBox, true)) {
                    continue outer;
                }
            }
            if (rearrangeWalls(state, blankCells, playerCells, scoreBox, false)) {
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
        final int[] blankCells,
        final int[] playerCells,
        final int[] scoreBox,
        final boolean isWalkAcceptable
    ) throws KilledException {
        for (int blankIndex = 0; blankIndex < blankCells.length; blankIndex += 1) {
            for (int playerIndex = 0; playerIndex < playerCells.length; playerIndex += 1) {
                checkAlive();
                final int blankCell = blankCells[blankIndex];
                final int playerCell = playerCells[playerIndex];
                state[blankCell] = Tile.PLAYER_WALL;
                state[playerCell] = Tile.BLANK;
                blankCells[blankIndex] = playerCell;
                playerCells[playerIndex] = blankCell;
                final int newScore = evaluator.evaluate(state);
                if (newScore > scoreBox[0]) {
                    scoreBox[0] = newScore;
                    return true;
                }
                if (isWalkAcceptable && newScore == scoreBox[0]) {
                    continue;
                }
                state[blankCell] = Tile.BLANK;
                state[playerCell] = Tile.PLAYER_WALL;
                blankCells[blankIndex] = blankCell;
                playerCells[playerIndex] = playerCell;
            }
        }
        return false;
    }

    private static int[] getCellsWhere(final Tile[] state, final Tile tile) {
        return IntArrays.ofRangeWhere(0, state.length, index -> state[index] == tile);
    }
}
