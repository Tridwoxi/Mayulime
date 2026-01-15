package think.stra;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;
import think.Solver;
import think.ana.Snake;
import think.ana.Tools;
import think.repr.Cell;
import think.repr.Problem;

/**
    Guess blindly. This strategy exists as a proof of concept and should be removed in
    a later version. It is not efficient and usually takes minutes to tie the best
    human score on a Simple.
 */
public final class Blind implements Runnable {

    private final Problem problem;

    public Blind(final Problem problem) {
        this.problem = problem;
    }

    @Override
    public void run() {
        while (true) {
            final HashSet<Cell> guess = guess();
            final int eval = Snake.evaluate(problem, guess);
            if (Solver.beatsBest(eval)) {
                Solver.claimImprovement(problem, guess, eval);
            }
        }
    }

    private HashSet<Cell> guess() {
        return Tools.randomly(new ArrayList<>(problem.getEmptyCells()))
            .limit(problem.getNumRubbers())
            .collect(Collectors.toCollection(HashSet::new));
    }
}
