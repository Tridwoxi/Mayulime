package think.stra;

import java.util.function.Supplier;
import think.ana.Manipulate;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;

/**
    Proof of concept, since it's nice to see some player walls assigned. This strategy
    is inefficient and often takes minutes to tie the best human score on a Simple.
 */
public final class RandomGuesser extends Strategy {

    public RandomGuesser(
        final Considerer considerer,
        final Supplier<Integer> scorer,
        final Problem problem
    ) {
        super(considerer, scorer, problem, "guess randomly");
    }

    @Override
    protected void solve() throws KilledException {
        while (true) {
            checkAlive();
            consider(maximalSplatter());
        }
    }

    private Grid<Feature> maximalSplatter() {
        final Grid<Feature> solution = getProblem().getAnotherInitial();
        Manipulate.splatter(solution, getProblem().getPlayerWallSupply());
        return solution;
    }
}
