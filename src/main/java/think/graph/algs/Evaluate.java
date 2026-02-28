package think.graph.algs;

import think.domain.repr.Board;
import think.domain.repr.Board.Feature;
import think.domain.repr.Puzzle;
import think.graph.Graph;
import think.graph.algs.Search.Fill;
import think.graph.impl.GridGraph.Cell;

/**
    Pathery Snake simulator. Produces a score for a given board configuration.
 */
public final class Evaluate {

    private Evaluate() {}

    public static int evaluate(final Puzzle puzzle, final Board board) {
        // TODO: This ought to return a record exposing segments so the caller learns more.
        int score = 0;
        Cell start = null;
        final Graph<Cell, Feature, Integer> graph = board.getBacking();
        for (final Cell finish : puzzle.getCheckpoints()) {
            if (start == null) {
                start = finish;
                continue;
            }
            final Fill<Cell, Feature, Integer> fill = Search.breadthFirst(graph, start);
            if (!fill.isReachable(finish)) {
                return 0;
            }
            score += fill.getPathTo(finish).edges().size();
            start = finish;
        }
        return score;
    }
}
