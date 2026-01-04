package think;

import app.Main;
import think.representation.Board;

public class Core {

    public Core(Board board) {
        Main.recieveUpdate(board, 0, board.getMaxRubbers());
    }
}
