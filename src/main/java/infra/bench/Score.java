package infra.bench;

import java.util.Comparator;
import java.util.List;
import think.manager.Proposal;

public final class Score {

    public record Report(String bestProposal, int score, long elapsedMillis) {}

    public static Report createReport(final long startTimeMillis, final List<Proposal> proposals) {
        if (proposals.isEmpty()) {
            return new Report("failure", -1, -1);
        }
        final Proposal best = proposals
            .stream()
            .max(Comparator.comparingInt(Proposal::getScore))
            .orElseThrow();
        return new Report(
            best.getSubmitter(),
            best.getScore(),
            best.getCreatedAtMillis() - startTimeMillis
        );
    }
}
