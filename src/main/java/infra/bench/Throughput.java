package infra.bench;

import java.util.List;
import think.manager.Proposal;

public final class Throughput {

    public record Report(long numProposals) {}

    private Throughput() {}

    public static List<Report> createReports(
        final long startTimeNanos,
        final List<Proposal> proposals
    ) {
        return List.of(new Report(proposals.size()));
    }
}
