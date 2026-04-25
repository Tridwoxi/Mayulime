package infra.bench;

import java.util.List;
import think.manager.Proposal;

public final class Throughput {

    public record Report(long numProposals) {}

    public static final class Context {

        private long count;
    }

    private Throughput() {}

    public static Context initialContext() {
        return new Context();
    }

    public static Context reduce(final Context context, final Proposal proposal) {
        context.count += 1L;
        return context;
    }

    public static List<Report> createReports(final Context context) {
        return List.of(new Report(context.count));
    }
}
