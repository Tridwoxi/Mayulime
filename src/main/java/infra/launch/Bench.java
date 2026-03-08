package infra.launch;

import infra.output.Logging;

/**
    Development-only headless launch point. Runs the solver specified by its first command line
    argument against Huge1 (a particularly difficult map) as a benchmark.
 */
public final class Bench {

    private static final String HUGE1_MAPCODE = """
        20.19.47.Huge1...:12,r1.1,r1.2,r1.3,r1.9,r1.35,r1.,r1.35,r1.,r1.21,r1.5,r1.3,c1.2,r1.4,r1.4,c3.12,r1.8,r1.5,r1.11,s1.3,r1.,r1.4,r1.7,r1.5,r1.7,r1.17,f1.2,r1.1,r1.2,r1.7,r1.11,r1.,r1.5,r1.22,r1.5,r1.3,r1.,r1.1,r1.6,c2.,r1.2,r1.10,r1.1,r1.,r1.4,r1.1,r1.3,r1.8,r1.3,r1.""";
    private static final int NUM_TRIALS = 10;
    private static final long RUNTIME_MS = 10_000;

    private Bench() {}

    public static void main(final String[] args) {
        Logging.announcement("Launch point: Bench");
    }
}
