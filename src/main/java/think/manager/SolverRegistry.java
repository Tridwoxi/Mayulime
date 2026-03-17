package think.manager;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum SolverRegistry {
    BASELINE,
    RANDOM,
    CLIMBV1;

    public static final class NoSuchSolverException extends Exception {}

    public static SolverRegistry fromString(final String name) throws NoSuchSolverException {
        for (final SolverRegistry candidate : SolverRegistry.values()) {
            if (candidate.toString().equalsIgnoreCase(name.strip())) {
                return candidate;
            }
        }
        throw new NoSuchSolverException();
    }

    public static String prettyNameAll() {
        return Arrays.stream(SolverRegistry.values())
            .map(SolverRegistry::toString)
            .collect(Collectors.joining("|"));
    }
}
