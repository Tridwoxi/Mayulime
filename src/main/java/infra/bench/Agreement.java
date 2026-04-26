package infra.bench;

import java.util.List;
import java.util.stream.Stream;
import think.common.StandardEvaluator;
import think.manager.Proposal;

public final class Agreement {

    public record Report(int topScore, int achievedBy, int totalProposals, double fraction) {}

    private Agreement() {}

    public static List<Report> createReports(final Stream<Proposal> proposals) {
        int topScore = StandardEvaluator.NO_PATH_EXISTS;
        int achievedBy = 0;
        int totalProposals = 0;
        for (final Proposal proposal : (Iterable<Proposal>) proposals::iterator) {
            final int score = proposal.getScore();
            if (score > topScore) {
                topScore = score;
                achievedBy = 1;
            } else if (score == topScore) {
                achievedBy += 1;
            }
            totalProposals += 1;
        }
        final double fraction = totalProposals == 0 ? 0.0 : (double) achievedBy / totalProposals;
        return List.of(new Report(topScore, achievedBy, totalProposals, fraction));
    }
}
