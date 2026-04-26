package infra.bench;

import java.util.List;
import java.util.stream.Stream;
import think.manager.Proposal;

public final class Throughput {

    public record Report(long numProposals) {}

    private Throughput() {}

    public static List<Report> createReports(final Stream<Proposal> proposals) {
        return List.of(new Report(proposals.count()));
    }
}
