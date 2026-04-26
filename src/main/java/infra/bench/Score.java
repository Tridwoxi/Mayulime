package infra.bench;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;
import think.domain.codec.Serializer;
import think.manager.Proposal;

public final class Score {

    public record Report(String bestProposal, int score, long elapsedMillis) {}

    private Score() {}

    public static List<Report> createReports(final Stream<Proposal> proposals) {
        final long startTimeNanos = System.nanoTime();
        Proposal best = null;
        for (final Proposal proposal : (Iterable<Proposal>) proposals::iterator) {
            if (best == null || proposal.getScore() > best.getScore()) {
                best = proposal;
            }
        }

        if (best == null) {
            return List.of();
        }
        return List.of(
            new Report(
                Serializer.serialize(best.getPuzzle(), best.getState()),
                best.getScore(),
                Duration.ofNanos(best.getCreatedAtNanos() - startTimeNanos).toMillis()
            )
        );
    }
}
