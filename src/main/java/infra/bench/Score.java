package infra.bench;

import java.util.Comparator;
import java.util.List;
import think.domain.codec.Serializer;
import think.manager.Proposal;

public final class Score {

    public record Report(String bestProposal, int score, long elapsedMillis) {}

    private Score() {}

    public static List<Report> createReports(
        final long startTimeNanos,
        final List<Proposal> proposals
    ) {
        if (proposals.isEmpty()) {
            return List.of();
        }
        final Proposal best = proposals
            .stream()
            .max(Comparator.comparingInt(Proposal::getScore))
            .orElseThrow();
        final Report report = new Report(
            Serializer.serialize(best.getPuzzle(), best.getState()),
            best.getScore(),
            (best.getCreatedAtNanos() - startTimeNanos) / 1_000_000L
        );
        return List.of(report);
    }
}
