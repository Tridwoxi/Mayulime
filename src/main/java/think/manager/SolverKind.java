package think.manager;

public enum SolverKind {
    BASELINE,
    RANDOM,
    CLIMBV1;

    public static final class NoSuchSolverException extends Exception {}

    public static SolverKind parse(final String name) throws NoSuchSolverException {
        for (final SolverKind candidate : SolverKind.values()) {
            if (candidate.toString().equalsIgnoreCase(name.strip())) {
                return candidate;
            }
        }
        throw new NoSuchSolverException();
    }
}
