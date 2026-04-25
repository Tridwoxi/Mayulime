package infra.bench;

import java.util.List;
import think.common.StandardEvaluator;
import think.manager.Proposal;

public final class Agreement {

    public record Report(int topScore, int achievedBy, int totalProposals, double fraction) {}

    public static final class Context {

        private int topScore = StandardEvaluator.NO_PATH_EXISTS;
        private int achievedBy;
        private int totalProposals;
    }

    private Agreement() {}

    public static Context initialContext() {
        return new Context();
    }

    public static Context reduce(final Context context, final Proposal proposal) {
        final int score = proposal.getScore();
        if (score > context.topScore) {
            context.topScore = score;
            context.achievedBy = 1;
        } else if (score == context.topScore) {
            context.achievedBy += 1;
        }
        context.totalProposals += 1;
        return context;
    }

    public static List<Report> createReports(final Context context) {
        final double fraction =
            context.totalProposals == 0
                ? 0.0
                : (double) context.achievedBy / context.totalProposals;
        return List.of(
            new Report(context.topScore, context.achievedBy, context.totalProposals, fraction)
        );
    }
}
