package infra.bench;

import java.util.List;
import think.domain.codec.Serializer;
import think.manager.Proposal;

public final class Score {

    public record Report(String bestProposal, int score, long elapsedMillis) {}

    public static final class Context {

        private final long startTimeNanos = System.nanoTime();
        private Proposal best = null;
    }

    private Score() {}

    public static Context initialContext() {
        return new Context();
    }

    public static Context reduce(final Context context, final Proposal proposal) {
        if (context.best == null || proposal.getScore() > context.best.getScore()) {
            context.best = proposal;
        }
        return context;
    }

    public static List<Report> createReports(final Context context) {
        if (context.best == null) {
            return List.of();
        }
        final Proposal best = context.best;
        return List.of(
            new Report(
                Serializer.serialize(best.getPuzzle(), best.getState()),
                best.getScore(),
                (best.getCreatedAtNanos() - context.startTimeNanos) / 1_000_000L
            )
        );
    }
}
