package infra.bench;

import infra.logging.Logger;
import think.common.StandardEvaluator;
import think.domain.codec.Serializer;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.ints.IntArrays;
import think.manager.Proposal;

/**
   A solver is locally optimal if there is no better solution in a neighborhood of 1-wall
   placements and moves. Multiple maps may be necessary for testing. Small1 is a good candidate
   since the baseline is not locally optimal.
*/
public final class Optimality implements Runnable {

    private final Params params;
    private long numProposals;
    private long numOptimal;

    public Optimality(final Params params) {
        this.params = params;
    }

    @Override
    public void run() {
        params.execute(this::accept, this::report);
    }

    private void accept(final Proposal proposal, final long elapsedMs) {
        numProposals += 1L;
        // Testing needs to happen during run because it is slow and can be used to throttle.
        if (isLocallyOptimal(proposal)) {
            numOptimal += 1L;
        } else {
            final String mapCode = Serializer.serialize(params.puzzle(), proposal.getState());
            Logger.results(
                "Not locally optimal (score %d at %d ms): %s",
                proposal.getScore(),
                elapsedMs,
                mapCode
            );
        }
    }

    private void report() {
        final double fraction = numProposals == 0L ? 0.0 : (double) numOptimal / numProposals;
        Logger.results("Locally optimal: %d of %d proposals", numOptimal, numProposals);
        Logger.results("Fraction: %f", fraction);
    }

    // == Below: ClimbSolver-like machinery. ==

    private boolean isLocallyOptimal(final Proposal proposal) {
        final Puzzle puzzle = proposal.getPuzzle();
        final Tile[] state = proposal.getState();
        final int score = proposal.getScore();
        final StandardEvaluator evaluator = new StandardEvaluator(puzzle);

        final int[] blankCells = cellsWhere(state, Tile.BLANK);
        final int[] playerCells = cellsWhere(state, Tile.PLAYER_WALL);
        final int remainingBudget = puzzle.getBlockingBudget() - countPlaced(puzzle, state);

        if (remainingBudget > 0 && canPlaceImproving(evaluator, state, blankCells, score)) {
            return false;
        }
        return !canSwapImproving(evaluator, state, blankCells, playerCells, score);
    }

    private static boolean canPlaceImproving(
        final StandardEvaluator evaluator,
        final Tile[] state,
        final int[] blankCells,
        final int score
    ) {
        for (final int cell : blankCells) {
            state[cell] = Tile.PLAYER_WALL;
            final int newScore = evaluator.evaluate(state);
            state[cell] = Tile.BLANK;
            if (newScore > score) {
                return true;
            }
        }
        return false;
    }

    private static boolean canSwapImproving(
        final StandardEvaluator evaluator,
        final Tile[] state,
        final int[] blankCells,
        final int[] playerCells,
        final int score
    ) {
        for (final int blankCell : blankCells) {
            for (final int playerCell : playerCells) {
                state[blankCell] = Tile.PLAYER_WALL;
                state[playerCell] = Tile.BLANK;
                final int newScore = evaluator.evaluate(state);
                state[blankCell] = Tile.BLANK;
                state[playerCell] = Tile.PLAYER_WALL;
                if (newScore > score) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int countPlaced(final Puzzle puzzle, final Tile[] state) {
        final Tile[] initial = puzzle.getTiles();
        int count = 0;
        for (int index = 0; index < state.length; index += 1) {
            if (initial[index] == Tile.BLANK && state[index] == Tile.PLAYER_WALL) {
                count += 1;
            }
        }
        return count;
    }

    private static int[] cellsWhere(final Tile[] state, final Tile tile) {
        return IntArrays.ofRangeWhere(0, state.length, index -> state[index] == tile);
    }
}
