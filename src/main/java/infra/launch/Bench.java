package infra.launch;

import infra.output.Logging;

/**
    Benchmark runner to examine the quality of solver implementations.
 */
public final class Bench {

    private Bench() {}

    public static void main(final String[] args) {
        final Bench benchmark = new Bench();
        Logging.announcement("Launch point: benchmark %s", benchmark);
    }
}
