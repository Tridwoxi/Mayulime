package think.stra;

import app.Main;
import java.util.HashSet;
import think.repr.Problem;

public final class Blind {

    public Blind(final Problem problem) {
        Main.recieve(problem, new HashSet<>(0), 0);
    }
}
