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
    Guess blindly. It'll work eventually, trust!! Often capable of tying the best
    human score on simples given a minute or two.
 */
public final class Blind {

    private final Problem problem;

    public Blind(final Problem problem) {
        this.problem = problem;
        run();
    }

    private void run() {
        while (true) {
            final HashSet<Cell> guess = guess();
            final int eval = Snake.eval(problem, guess);
            if (Solver.beatsBest(eval)) {
                Solver.claimSolution(problem, guess, eval);
            }
        }
    }

    private HashSet<Cell> guess() {
        return Tools.randomly(new ArrayList<>(problem.getEmptyCells()))
            .limit(problem.getNumRubbers())
            .collect(Collectors.toCollection(HashSet::new));
    }
}
