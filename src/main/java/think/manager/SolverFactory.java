package think.manager;

import think.domain.model.Puzzle;
import think.solvers.Solver;
import think.solvers.Solver.ProposedSolution;
import think.solvers.local.ClimbV1Solver;
import think.solvers.naive.BaselineSolver;
import think.solvers.naive.RandomSolver;

final class SolverFactory {

    private final ProposedSolution listener;
    private final Puzzle puzzle;

    SolverFactory(final ProposedSolution listener, final Puzzle puzzle) {
        this.listener = listener;
        this.puzzle = puzzle;
    }

    Solver create(final SolverRegistry registry) {
        return switch (registry) {
            case BASELINE -> new BaselineSolver(listener, puzzle);
            case RANDOM -> new RandomSolver(listener, puzzle);
            case CLIMBV1 -> new ClimbV1Solver(listener, puzzle);
        };
    }
}
