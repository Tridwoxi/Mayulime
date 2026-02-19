package think.solve;

import think.repr.Problem;

/**
    Hack used by {@link think.Manager} to send baseline results.
 */
public final class BaselineSolver extends Solver {

    public BaselineSolver(final ProposedSolutionListener listener, final Problem problem) {
        super(listener, problem);
    }

    @Override
    protected void solve() throws KilledException {
        checkAlive();
        proposeSolution(getProblem().getBlankSolution());
    }
}
