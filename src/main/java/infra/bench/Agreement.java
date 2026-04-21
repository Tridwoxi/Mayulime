package infra.bench;

import java.util.List;
import think.manager.Proposal;

public final class Agreement {

    public record Report(int topScore, int achievedBy, int totalProposals, double fraction) {}

    public static List<Report> createReports(
        final long startTimeMillis,
        final List<Proposal> proposals
    ) {
        int topScore = 0;
        int achievedBy = 0;
        int totalProposals = 0;
        for (final Proposal proposal : proposals) {
            if (proposal.getScore() > topScore) {
                topScore = proposal.getScore();
                achievedBy = 1;
            } else if (proposal.getScore() == topScore) {
                achievedBy += 1;
            }
            totalProposals += 1;
        }
        final Report report = new Report(
            topScore,
            achievedBy,
            totalProposals,
            totalProposals == 0 ? 0.0 : (double) achievedBy / totalProposals
        );
        return List.of(report);
    }
}
