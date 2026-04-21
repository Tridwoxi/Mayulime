package infra.bench;

import java.util.List;
import think.common.StandardEvaluator;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.ints.IntArrays;
import think.manager.Proposal;

/**
   A solver is locally optimal if there is no better solution in a neighborhood of 1-wall
   placements and moves. Multiple maps may be necessary for testing. Small1 is a good candidate
   since the baseline is not locally optimal.
*/
public final class Optimality {

    public record Report(long numProposals, long numOptimal, double fraction) {}

    private Optimality() {}

    public static List<Report> createReports(
        final long startTimeNanos,
        final List<Proposal> proposals
    ) {
        long numOptimal = 0L;
        for (final Proposal proposal : proposals) {
            if (isLocallyOptimal(proposal)) {
                numOptimal += 1L;
            }
        }
        final long numProposals = proposals.size();
        final double fraction = numProposals == 0L ? 0.0 : (double) numOptimal / numProposals;
        return List.of(new Report(numProposals, numOptimal, fraction));
    }

    // == Below: ClimbSolver-like machinery. ==

    private static boolean isLocallyOptimal(final Proposal proposal) {
        final Puzzle puzzle = proposal.getPuzzle();
        final Tile[] state = proposal.getState();
        final int score = proposal.getScore();
        final StandardEvaluator evaluator = new StandardEvaluator(puzzle);

        final int[] blankCells = cellsWhere(state, Tile.BLANK);
        final int[] playerCells = cellsWhere(state, Tile.PLAYER_WALL);
        final int remainingBudget = puzzle.blockingBudget() - countPlaced(puzzle, state);

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
        final Tile[] initial = puzzle.tiles();
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
