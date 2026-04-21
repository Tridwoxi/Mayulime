package infra.bench;

import java.util.List;
import java.util.TreeMap;
import think.manager.Proposal;

public final class Distribution {

    public record Report(int score, int count) {}

    private Distribution() {}

    public static List<Report> createReports(
        final long startTimeMillis,
        final List<Proposal> proposals
    ) {
        final TreeMap<Integer, Integer> counts = new TreeMap<>();
        for (final Proposal proposal : proposals) {
            final int score = proposal.getScore();
            counts.merge(score, 1, Integer::sum);
        }
        return counts
            .entrySet()
            .stream()
            .map(entry -> new Report(entry.getKey(), entry.getValue()))
            .toList();
    }
}
