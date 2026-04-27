package infra.bench;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import think.manager.Proposal;

public final class First {

    public record Report(long elapsedNanos) {}

    private First() {}

    public static List<Report> createReports(final Stream<Proposal> proposals) {
        final Optional<Proposal> first = proposals.findFirst();
        if (first.isEmpty()) {
            return List.of();
        }
        final long elapsedNanos = first.orElseThrow().getCreatedAfter().toNanos();
        return List.of(new Report(elapsedNanos));
    }
}
