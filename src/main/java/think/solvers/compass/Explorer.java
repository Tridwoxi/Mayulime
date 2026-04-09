package think.solvers.compass;

import think.common.DistanceFinder;
import think.common.StandardEvaluator;
import think.domain.model.Puzzle;

final class Explorer {

    private final Puzzle puzzle;
    private final StandardEvaluator evaluator;
    private final DistanceFinder finder;

    Explorer(final Puzzle puzzle) {
        this.puzzle = puzzle;
        this.evaluator = new StandardEvaluator(puzzle);
        this.finder = new DistanceFinder(puzzle);
    }
}
