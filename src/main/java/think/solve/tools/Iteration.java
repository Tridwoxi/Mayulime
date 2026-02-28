package think.solve.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class Iteration {

    private Iteration() {}

    public static <T> Stream<T> randomly(final Collection<T> items) {
        final List<T> view = new ArrayList<>(items);
        Collections.shuffle(view);
        return view.stream();
    }
}
