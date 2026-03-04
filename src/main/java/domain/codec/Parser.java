package domain.codec;

import domain.model.Puzzle;

/**
    This class constructs a {@link Puzzle} from a string, or throws a {@link BadMapCodeException}
    if it is unable (either because the input is malformed or contains unsupported features).

    Simplified Pathery MapCode (please see https://www.pathery.com/mapeditor) grammar:

    <pre>
    MapCode         -> Metadata : Maze
    Metadata        -> NumCols . NumRows . BlockingBudget . Name . . .
    NumCols         -> int
    NumRows         -> int
    BlockingBudget  -> int
    Name            -> string
    Maze            -> Token Maze | nothing
    Token           -> Skip , Kind Order .
    Skip            -> int | nothing
    Kind            -> s | f | r | c
    Order           -> int
    </pre>

    Integers contain only digits and are strictly positive. {@code |} and {@code nothing} are a
    metasymbols representing alternation and an empty production. There are kinds beyond those
    defined here, and can be learned of by looking at the map editor. Name must not contain the
    {@code .} symbol.

    This parser also enforces semantic correctness: features stay within bounds, checkpoints must
    have unique orders, and blocking budget cannot exceed blank cells. Pathery supports variants
    with multiple starts / finishes / checkpoints, but we do not yet.
 */
public final class Parser {

    public static final class BadMapCodeException extends Exception {}

    private Parser() {}

    public static Puzzle parse(final String mapCode) throws BadMapCodeException {
        return null;
    }
}
