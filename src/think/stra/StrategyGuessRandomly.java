package think.stra;

import think.ana.Manipulate;
import think.ana.Pathfind;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;

/**
    Proof of concept. Not intended for use, except perhaps as a benchmark.

    <pre>
    forever {
        guess randomly;
        evaluate;
    }
    </pre>
 */
public final class StrategyGuessRandomly extends Strategy {

    public StrategyGuessRandomly(
        final ProposedSolutionListener proposedSolutionListener,
        final TopScoreSupplier topScoreSupplier,
        final Problem problem
    ) {
        super(proposedSolutionListener, topScoreSupplier, problem);
    }

    @Override
    protected void solve() throws KilledException {
        while (true) {
            checkAlive();
            final Grid<Feature> solution = createMaximalSplatter();
            final int score = Pathfind.evaluate(getProblem(), solution);
            if (score > getTopScore()) {
                proposeSolution(solution, score);
            }
        }
    }

    private Grid<Feature> createMaximalSplatter() {
        final Grid<Feature> solution = getProblem().getAnotherInitial();
        Manipulate.splatter(solution, getProblem().getPlayerWallSupply());
        return solution;
    }
}
