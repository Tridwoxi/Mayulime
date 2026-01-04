package think;

import app.Main;
import java.util.HashSet;
import think.repr.Board;
import think.repr.Point;

public class Core {

    public Core(Board board) {
        HashSet<Point> rubberAssignment = new HashSet<>();
        Main.recieveUpdate(board, rubberAssignment, 0);
    }
}
