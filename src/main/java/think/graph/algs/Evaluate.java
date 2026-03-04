package think.graph.algs;

import domain.model.Board;
import domain.model.Puzzle;
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
        final Graph<Cell, Object, Integer> graph = board.getTraversalGraph();
        for (final Cell finish : puzzle.getCheckpointOrder()) {
            if (start == null) {
                start = finish;
                continue;
            }
            final Fill<Cell, Object, Integer> fill = Search.breadthFirst(graph, start);
            if (!fill.isReachable(finish)) {
                return 0;
            }
            score += fill.getPathTo(finish).edges().size();
            start = finish;
        }
        return score;
    }
}
