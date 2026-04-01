package think.solvers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import think.domain.model.Puzzle;
import think.manager.Proposal;
import think.solvers.global.NoveltySolver;
import think.solvers.local.AnnealSolver;
import think.solvers.local.ChokepointSolver;
import think.solvers.local.ClimbSolver;
import think.solvers.local.IdentitySolver;
import think.solvers.local.IntersectSolver;
import think.solvers.local.UncoverSolver;
import think.solvers.local.WalkSolver;
import think.solvers.naive.BaselineSolver;
import think.solvers.naive.RandomSolver;

public enum SolverKind {
    ANNEAL,
    BASELINE,
    CHOKEPOINT,
    CLIMB,
    IDENTITY,
    INTERSECT,
    NOVELTY,
    RANDOM,
    UNCOVER,
    WALK;

    public static final class NoSuchSolverException extends Exception {}

    public Solver create(final Consumer<Proposal> listener, final Puzzle puzzle) {
        return switch (this) {
            case ANNEAL -> new AnnealSolver(listener, puzzle);
            case BASELINE -> new BaselineSolver(listener, puzzle);
            case CHOKEPOINT -> new ChokepointSolver(listener, puzzle);
            case CLIMB -> new ClimbSolver(listener, puzzle);
            case IDENTITY -> new IdentitySolver(listener, puzzle);
            case INTERSECT -> new IntersectSolver(listener, puzzle);
            case NOVELTY -> new NoveltySolver(listener, puzzle);
            case RANDOM -> new RandomSolver(listener, puzzle);
            case UNCOVER -> new UncoverSolver(listener, puzzle);
            case WALK -> new WalkSolver(listener, puzzle);
        };
    }

    public static SolverKind parse(final String name) throws NoSuchSolverException {
        for (final SolverKind candidate : SolverKind.values()) {
            if (candidate.toString().equalsIgnoreCase(name.strip())) {
                return candidate;
            }
        }
        throw new NoSuchSolverException();
    }

    public static List<SolverKind> asList() {
        return Arrays.asList(values());
    }

    public static SolverKind getBest() {
        return INTERSECT;
    }
}
