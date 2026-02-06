package think.stra;

import think.ana.Pathfind;
import think.repr.Problem;

/**
    Hack used by {@link think.Manager} to send baseline results.
 */
public final class StrategyBaseline extends Strategy {

    public StrategyBaseline(
        final ProposedSolutionListener proposedSolutionListener,
        final TopScoreSupplier topScoreSupplier,
        final Problem problem
    ) {
        super(proposedSolutionListener, topScoreSupplier, problem);
    }

    @Override
    protected void solve() throws KilledException {
        checkAlive();
        proposeSolution(
            getProblem().getCachedInitial(),
            Pathfind.evaluate(getProblem(), getProblem().getCachedInitial())
        );
    }
}
