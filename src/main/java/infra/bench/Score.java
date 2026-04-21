package infra.bench;

import java.util.Comparator;
import java.util.List;
import think.manager.Proposal;

public final class Score {

    public record Report(String bestProposal, int score, long elapsedMillis) {}

    public static List<Report> createReports(
        final long startTimeMillis,
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
            best.getSubmitter(),
            best.getScore(),
            best.getCreatedAtMillis() - startTimeMillis
        );
        return List.of(report);
    }
}
