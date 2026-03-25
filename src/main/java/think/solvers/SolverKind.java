package think.solvers;

import java.util.Arrays;
import java.util.List;

public enum SolverKind {
    BASELINE,
    RANDOM,
    CLIMB,
    IDENTITY,
    WALK;

    public static final class NoSuchSolverException extends Exception {}

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
}
