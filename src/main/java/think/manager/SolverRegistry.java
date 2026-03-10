package think.manager;

public enum SolverRegistry {
    BASELINE,
    RANDOM,
    CLIMBV1;

    public static final class NoSuchSolverException extends Exception {}

    public SolverRegistry fromString(final String name) throws NoSuchSolverException {
        for (final SolverRegistry candidate : SolverRegistry.values()) {
            if (candidate.toString().equalsIgnoreCase(name.strip())) {
                return candidate;
            }
        }
        throw new NoSuchSolverException();
    }
}
