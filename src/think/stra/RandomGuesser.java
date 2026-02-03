package think.stra;

import think.Manager;
import think.ana.Manipulate;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;

/**
    Proof of concept, since it's nice to see some player walls assigned. This strategy
    is inefficient and often takes minutes to tie the best human score on a Simple.
 */
public final class RandomGuesser extends Strategy {

    public RandomGuesser(final Problem problem) {
        super(problem, "guess randomly");
    }

    @Override
    protected void solve() throws KilledException {
        while (true) {
            checkAlive();
            Manager.getInstance().consider(this, getProblem(), maximalRandomSplatter());
        }
    }

    private Grid<Feature> maximalRandomSplatter() {
        final Grid<Feature> solution = getProblem().getAnotherInitial();
        Manipulate.splatter(solution, getProblem().getPlayerWallSupply());
        return solution;
    }
}
