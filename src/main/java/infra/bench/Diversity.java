package infra.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;
import think.common.PathTracer;
import think.domain.model.Tile;
import think.manager.Proposal;

public final class Diversity {

    public enum Kind {
        WALL_DISTANCE,
        PATH_DISTANCE,
    }

    public record Report(Kind kind, int distance, long count) {}

    private Diversity() {}

    public static List<Report> createReports(final Stream<Proposal> proposals) {
        final List<Proposal> list = proposals.toList();
        if (list.size() <= 1) {
            return List.of();
        }

        final PathTracer tracer = new PathTracer(list.getFirst().getPuzzle());
        final List<Tile[]> states = list.stream().map(Proposal::getState).toList();
        final List<int[]> paths = states.stream().map(tracer::trace).toList();

        final TreeMap<Integer, Long> wallCounts = new TreeMap<>();
        final TreeMap<Integer, Long> pathCounts = new TreeMap<>();
        for (int left = 0; left < states.size(); left += 1) {
            for (int right = left + 1; right < states.size(); right += 1) {
                wallCounts.merge(wallDistance(states.get(left), states.get(right)), 1L, Long::sum);
                pathCounts.merge(pathDistance(paths.get(left), paths.get(right)), 1L, Long::sum);
            }
        }

        final List<Report> reports = new ArrayList<>(wallCounts.size() + pathCounts.size());
        wallCounts.forEach((distance, count) ->
            reports.add(new Report(Kind.WALL_DISTANCE, distance, count))
        );
        pathCounts.forEach((distance, count) ->
            reports.add(new Report(Kind.PATH_DISTANCE, distance, count))
        );
        return reports;
    }

    private static int wallDistance(final Tile[] left, final Tile[] right) {
        int distance = 0;
        for (int index = 0; index < left.length; index += 1) {
            if ((left[index] == Tile.PLAYER_WALL) != (right[index] == Tile.PLAYER_WALL)) {
                distance += 1;
            }
        }
        return distance;
    }

    private static int pathDistance(final int[] left, final int[] right) {
        int distance = 0;
        for (int index = 0; index < left.length; index += 1) {
            distance += Math.abs(left[index] - right[index]);
        }
        return distance;
    }
}
