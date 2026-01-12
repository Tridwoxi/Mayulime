package think.stra;

import app.Main;
import java.util.HashSet;
import think.repr.Point;
import think.repr.Problem;

public class Blind {

    private Problem problem;

    public Blind(Problem problem) {
        this.problem = problem;
        Main.recieve(this.problem, new HashSet<Point>(), 0);
    }
}
