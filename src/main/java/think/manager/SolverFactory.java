package think.manager;

import java.util.Arrays;
import java.util.List;
import think.domain.model.Puzzle;
import think.solvers.Solver;
import think.solvers.Solver.ProposedSolution;
import think.solvers.local.ClimbV1Solver;
import think.solvers.naive.BaselineSolver;
import think.solvers.naive.RandomSolver;

public final class SolverFactory {

    public static final class NoSuchSolverException extends Exception {}

    public enum Registry {
        BASELINE,
        RANDOM,
        CLIMBV1,
    }

    private final ProposedSolution listener;
    private final Puzzle puzzle;

    public SolverFactory(final ProposedSolution listener, final Puzzle puzzle) {
        this.listener = listener;
        this.puzzle = puzzle;
    }

    public Solver create(final String name) throws NoSuchSolverException {
        for (final Registry candidate : Registry.values()) {
            if (candidate.toString().equalsIgnoreCase(name.strip())) {
                return create(candidate);
            }
        }
        throw new NoSuchSolverException();
    }

    public Solver create(final Registry registry) {
        return switch (registry) {
            case BASELINE -> new BaselineSolver(listener, puzzle);
            case RANDOM -> new RandomSolver(listener, puzzle);
            case CLIMBV1 -> new ClimbV1Solver(listener, puzzle);
        };
    }

    public List<Solver> createOneOfEach() {
        return Arrays.stream(Registry.values()).map(this::create).toList();
    }
}
