package think.solvers.exact;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import think.common.IntArrays;
import think.common.StandardEvaluator;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.manager.Proposal;
import think.solvers.Solver;

public final class PruneSolver extends Solver {

    private final StandardEvaluator evaluator;
    private final int[] blankCells;

    public PruneSolver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.evaluator = new StandardEvaluator(puzzle);
        this.blankCells = getBlankCells();
    }

    @Override
    protected void solve() throws KilledException {
        // TODO: Implement.
        final Feature[] maze = getPuzzle().getFeatures();
        int topScore = StandardEvaluator.NO_PATH_EXISTS;

        final Deque<StackFrame> stack = new ArrayDeque<>(blankCells.length);
        stack.push(new StackFrame(0, 0, 0));
        while (stack.size() > 0) {
            checkAlive();
            final StackFrame frame = stack.pop();
            if (frame.numPlacedSoFar() > getPuzzle().getBlockingBudget()) {
                continue;
            }

            if (frame.placedThisFrameIndex() != StackFrame.NO_PLACEMENT) {
                maze[frame.placedThisFrameIndex()] = Feature.PLAYER_WALL;
                final int score = evaluator.evaluate(maze);
                if (score > topScore) {
                    topScore = score;
                    propose(maze);
                }
            }
        }
    }

    private int[] getBlankCells() {
        final Feature[] maze = getPuzzle().getFeatures();
        return IntArrays.ofRangeWhere(0, maze.length, index -> maze[index] == Feature.BLANK);
    }

    private record StackFrame(int numPlacedSoFar, int placedThisFrameIndex, int nextBlankIndex) {
        private static final int NO_PLACEMENT = -1;
    }
}
