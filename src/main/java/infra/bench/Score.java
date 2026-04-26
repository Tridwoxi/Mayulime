package infra.bench;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import think.domain.codec.Serializer;
import think.manager.Proposal;

public final class Score {

    public record Report(String bestProposal, int score, long elapsedMillis) {}

    private Score() {}

    public static List<Report> createReports(
        final long solveBeginNanos,
        final Stream<Proposal> proposals
    ) {
        return proposals
            .max(Comparator.comparingInt(Proposal::getScore))
            .map(best ->
                new Report(
                    Serializer.serialize(best.getPuzzle(), best.getState()),
                    best.getScore(),
                    Duration.ofNanos(best.getCreatedAtNanos() - solveBeginNanos).toMillis()
                )
            )
            .stream()
            .toList();
    }
}
