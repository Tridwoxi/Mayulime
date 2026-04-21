package unit;

import infra.bench.Agreement;
import infra.bench.Score;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.manager.Proposal;

public final class Bench {

    private static final Puzzle LINE = new Puzzle(
        "Line",
        new Tile[] { Tile.WAYPOINT, Tile.BLANK, Tile.WAYPOINT },
        1,
        3,
        new int[] { 0, 2 },
        1
    );

    @Test
    public void agreementHandlesBlockedProposals() {
        final Proposal blocked = new Proposal(
            "blocked",
            LINE,
            new Tile[] { Tile.WAYPOINT, Tile.PLAYER_WALL, Tile.WAYPOINT }
        );

        final Agreement.Report report = Agreement.createReports(0L, List.of(blocked, blocked)).getFirst();

        Assertions.assertEquals(-1, report.topScore());
        Assertions.assertEquals(2, report.achievedBy());
        Assertions.assertEquals(2, report.totalProposals());
        Assertions.assertEquals(1.0, report.fraction());
    }

    @Test
    public void scoreReportsSerializedBestProposal() {
        final Proposal blocked = new Proposal(
            "blocked",
            LINE,
            new Tile[] { Tile.WAYPOINT, Tile.PLAYER_WALL, Tile.WAYPOINT }
        );
        final Proposal passable = new Proposal("passable", LINE, LINE.tiles());

        final Score.Report report = Score.createReports(0L, List.of(blocked, passable)).getFirst();

        Assertions.assertEquals("3.1.1.Line...:,s1.1,f1.", report.bestProposal());
        Assertions.assertEquals(2, report.score());
    }
}
