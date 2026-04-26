package infra.bench;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;
import think.manager.Proposal;

public final class Distribution {

    public record Report(int score, int count) {}

    private Distribution() {}

    public static List<Report> createReports(final Stream<Proposal> proposals) {
        final TreeMap<Integer, Integer> counts = new TreeMap<>();
        for (final Proposal proposal : (Iterable<Proposal>) proposals::iterator) {
            counts.merge(proposal.getScore(), 1, Integer::sum);
        }
        return counts
            .entrySet()
            .stream()
            .map(entry -> new Report(entry.getKey(), entry.getValue()))
            .toList();
    }
}
