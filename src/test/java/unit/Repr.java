package unit;

import domain.codec.Parser;
import domain.codec.Parser.BadMapCodeException;
import domain.model.Board;
import domain.model.Display;
import domain.model.Puzzle;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import think.graph.impl.GridGraph.Cell;

public final class Repr {

    private static final String SMALL1_MAPCODE = """
        13.6.7.Small1...:,r3.11,r3.,r3.2,r1.8,r3.,r3.8,r1.2,f1.,s1.11,r3.,r3.2,r1.,r1.4,r1.2,r3.,r3.2,c1.8,r3.""";

    @Test
    public void puzzleFaithfulToMapCode() throws BadMapCodeException {
        final Puzzle puzzle = Parser.parse(SMALL1_MAPCODE);
        final Set<Cell> empty = puzzle.getOriginallyEmpty();
        final Set<Cell> missing = puzzle.getOriginallyMissing();
        final Set<Cell> checkpoint = puzzle.getOriginallyCheckpoint();

        Assertions.assertEquals("Small1", puzzle.getName());
        Assertions.assertEquals(6, puzzle.getNumRows());
        Assertions.assertEquals(13, puzzle.getNumCols());
        Assertions.assertEquals(7, puzzle.getWallBudget());
        Assertions.assertEquals(78, puzzle.getAllPossibleCells().size());

        Assertions.assertEquals(60, empty.size());
        Assertions.assertEquals(15, missing.size());
        Assertions.assertEquals(3, checkpoint.size());

        Assertions.assertEquals(3, puzzle.getCheckpointOrder().size());
        Assertions.assertEquals(new Cell(5, 3), puzzle.getCheckpointOrder().get(1));

        Assertions.assertTrue(areDisjoint(empty, missing));
        Assertions.assertTrue(areDisjoint(empty, checkpoint));
        Assertions.assertTrue(areDisjoint(missing, checkpoint));
    }

    @Test
    public void puzzleKnowsValidBoard() throws BadMapCodeException {
        final Puzzle puzzle = Parser.parse(SMALL1_MAPCODE);
        final Board board = puzzle.getBoard();
        for (Cell cell : puzzle.getOriginallyEmpty()) {
            board.placeWall(cell);
            final boolean expected = board.getNumSpentWalls() <= puzzle.getWallBudget();
            Assertions.assertEquals(expected, puzzle.isValid(board));
        }
    }

    @Test
    public void puzzleUncorruptable() throws BadMapCodeException {
        final Puzzle puzzle = Parser.parse(SMALL1_MAPCODE);

        final Board boardCopy = puzzle.getBoard();
        final Cell spent = puzzle.getOriginallyEmpty().iterator().next();
        boardCopy.placeWall(spent);
        Assertions.assertFalse(puzzle.getBoard().isSpentWall(spent));

        final Set<Cell> emptied = puzzle.getOriginallyEmpty();
        emptied.clear();
        Assertions.assertFalse(puzzle.getOriginallyEmpty().isEmpty());

        Assertions.assertTrue(puzzle.getBoard() != puzzle.getBoard());
    }

    @Test
    public void displayKnowsNameAndKind() throws BadMapCodeException {
        final Puzzle puzzle = Parser.parse(SMALL1_MAPCODE);
        final Board board = puzzle.getBoard();

        final Cell playerWall = puzzle.getOriginallyEmpty().iterator().next();
        board.placeWall(playerWall);

        final Cell systemWall = puzzle.getOriginallyMissing().iterator().next();
        final List<Cell> checkpoints = puzzle.getCheckpointOrder();
        final Cell emptyCell = puzzle
            .getOriginallyEmpty()
            .stream()
            .filter(cell -> !cell.equals(playerWall))
            .findFirst()
            .orElseThrow(AssertionError::new);

        final Display display = new Display("tester", puzzle, board, 42);

        Assertions.assertEquals(Display.Kind.PLAYER_WALL, display.getKind(playerWall));
        Assertions.assertEquals(Display.Kind.SYSTEM_WALL, display.getKind(systemWall));
        Assertions.assertEquals(Display.Kind.EMPTY, display.getKind(emptyCell));
        Assertions.assertEquals(Display.Kind.CHECKPOINT, display.getKind(checkpoints.get(0)));
        Assertions.assertEquals(Display.Kind.CHECKPOINT, display.getKind(checkpoints.get(1)));
        Assertions.assertEquals(Display.Kind.CHECKPOINT, display.getKind(checkpoints.get(2)));

        Assertions.assertEquals("c0", display.getName(checkpoints.get(0)));
        Assertions.assertEquals("c1", display.getName(checkpoints.get(1)));
        Assertions.assertEquals("c2", display.getName(checkpoints.get(2)));
        Assertions.assertEquals("", display.getName(playerWall));
        Assertions.assertEquals("", display.getName(systemWall));
        Assertions.assertEquals("", display.getName(emptyCell));
    }

    @Test
    public void boardUnderstandsLegality() throws BadMapCodeException {
        final Class<IllegalArgumentException> expected = IllegalArgumentException.class;

        final Puzzle puzzle = Parser.parse(SMALL1_MAPCODE);
        final Board board = puzzle.getBoard();
        final Cell emptyCell = puzzle.getOriginallyEmpty().iterator().next();
        final Cell blockedCell = puzzle.getOriginallyMissing().iterator().next();

        Assertions.assertThrows(expected, () -> board.removeWall(emptyCell));
        Assertions.assertThrows(expected, () -> board.placeWall(blockedCell));
        board.placeWall(emptyCell);
        Assertions.assertThrows(expected, () -> board.placeWall(emptyCell));
        board.removeWall(emptyCell);
        Assertions.assertThrows(expected, () -> board.removeWall(emptyCell));
    }

    private static boolean areDisjoint(final Set<Cell> first, final Set<Cell> second) {
        return first.stream().noneMatch(second::contains);
    }
}
