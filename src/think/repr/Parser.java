package think.repr;

import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import think.repr.Cell.CellType;

/**
    Make board from string specification like (not whitespace-sensitive):

    {@snippet txt :
        num_rubbers;
        width;
        height;
        i,j,cell_type association(optional);
    }

    For example, a 4 by 4 board with a start checkpoint in the top left, brick in top right, and finish checkpoint in bottom right:

    {@snippet :
        2;
        4;
        4;
        0,0,c,1;
        0,3,b;
        3,3,c,2;
    }
 */
public final class Parser {

    private static final HashMap<String, CellType> legend; // <Abbreviation, Cell type>

    private Parser() {}

    public static Board parse(final String source) throws IllegalArgumentException {
        try (Scanner s = new Scanner(source).useDelimiter("\\s*;\\s*")) {
            final int numRubbers = s.nextInt();
            final int boundJ = s.nextInt(); // Width.
            final int boundI = s.nextInt(); // Height.
            final Cell[][] cells = new Cell[boundI][boundJ];
            for (int i = 0; i < boundI; i++) {
                for (int j = 0; j < boundJ; j++) {
                    cells[i][j] = new Cell(CellType.NOTHING, 0);
                }
            }
            while (s.hasNext()) {
                parseEntry(cells, s.next());
            }
            return new Board(cells, numRubbers);
        } catch (InputMismatchException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void parseEntry(final Cell[][] cells, final String entry) {
        if (entry.isBlank()) {
            return;
        }
        try (Scanner s = new Scanner(entry).useDelimiter("\\s*,\\s*")) {
            final int i = s.nextInt();
            final int j = s.nextInt();
            final String typeToken = s.next();
            final CellType type = legend.get(typeToken.trim().toUpperCase());
            if (type == null) {
                throw new IllegalArgumentException("Unknown cell type: " + typeToken);
            }
            if (type == CellType.RUBBER) {
                throw new IllegalArgumentException("Can't specify rubbers.");
            }
            if (i < 0 || i >= cells.length || j < 0 || j >= cells[0].length) {
                throw new IllegalArgumentException("Coordinates out of bounds.");
            }
            if (cells[i][j].type() != CellType.NOTHING) {
                throw new IllegalArgumentException("Cell specified twice.");
            }
            int association = 0;
            if (s.hasNext()) {
                association = s.nextInt();
            }
            cells[i][j] = new Cell(type, association);
        }
    }

    static {
        legend = new HashMap<>();
        legend.put("B", CellType.BRICK);
        legend.put("C", CellType.CHECKPOINT);
        legend.put("N", CellType.NOTHING);
        legend.put("R", CellType.RUBBER);
        legend.put("I", CellType.TELEPORT_IN);
        legend.put("O", CellType.TELEPORT_OUT);
    }
}
