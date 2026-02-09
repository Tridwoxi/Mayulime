package think.stra;

import think.repr.Problem;

/**
    Hack used by {@link think.Manager} to send baseline results.
 */
public final class StrategyBaseline extends Strategy {

    public StrategyBaseline(
        final ProposedSolutionListener listener,
        final Problem problem
    ) {
        super(listener, problem);
    }

    @Override
    protected void solve() throws KilledException {
        checkAlive();
        proposeSolution(getProblem().getCachedInitial());
    }
}
