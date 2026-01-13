package think.stra;

import app.Main;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;
import think.ana.Snake;
import think.ana.Tools;
import think.ana.Tools.Pair;
import think.repr.Cell;
import think.repr.Problem;

/**
    Guess blindly. It'll work eventually, trust!! Often capable of tying the best
    human score on simples given a minute or two.
 */
public final class Blind {

    private final Problem problem;
    private int bestScore;

    public Blind(final Problem problem) {
        this.problem = problem;
        this.bestScore = 0;
        run();
    }

    private void run() {
        while (true) {
            final HashSet<Cell> guess = guess();
            final int eval = eval(guess);
            if (eval > bestScore) {
                bestScore = eval;
                Main.recieve(problem, guess, eval);
            }
        }
    }

    private HashSet<Cell> guess() {
        return Tools.randomly(new ArrayList<>(problem.getEmptyCells()))
            .limit(problem.getNumRubbers())
            .collect(Collectors.toCollection(HashSet::new));
    }

    private int eval(final HashSet<Cell> rubbers) {
        int sum = 0;
        final Snake snake = new Snake();
        for (final Pair<Cell> p : Tools.pairwise(problem.getCheckpoints()).toList()) {
            final int length = snake.travel(problem, rubbers, p.a(), p.b()).length();
            if (length == 0) {
                return 0;
            } else {
                sum += length;
            }
        }
        return sum;
    }
}
