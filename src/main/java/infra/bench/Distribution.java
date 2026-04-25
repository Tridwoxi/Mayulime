package infra.bench;

import java.util.List;
import java.util.TreeMap;
import think.manager.Proposal;

public final class Distribution {

    public record Report(int score, int count) {}

    public static final class Context {

        private final TreeMap<Integer, Integer> counts = new TreeMap<>();
    }

    private Distribution() {}

    public static Context initialContext() {
        return new Context();
    }

    public static Context reduce(final Context context, final Proposal proposal) {
        context.counts.merge(proposal.getScore(), 1, Integer::sum);
        return context;
    }

    public static List<Report> createReports(final Context context) {
        return context.counts
            .entrySet()
            .stream()
            .map(entry -> new Report(entry.getKey(), entry.getValue()))
            .toList();
    }
}
