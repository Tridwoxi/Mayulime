package think.stra;

import think.repr.Problem;

public abstract class Strategy implements Runnable {

    private final Problem problem;
    private final String name;
    private volatile boolean keepGoing;

    public Strategy(final Problem problem, final String name) {
        this.problem = problem;
        this.name = name;
        this.keepGoing = true;
    }

    /**
        Generate solutions to the problem, and ask the manager to consider them.

        Concrete subclasses should do all non-trivial work in this method. It would be
        nice if this method returns shortly (ideally within 500 miliseconds) after its
        owning instance is killed, but failing to do so is merely an annoyance and not
        an error since all it does is waste compute. This method comes from {@link
        Runnable}.
     */
    @Override
    public abstract void run();

    public final void pleaseDie() {
        this.keepGoing = false;
    }

    public final String getName() {
        return name;
    }

    protected final boolean keepGoing() {
        return keepGoing;
    }

    protected final Problem getProblem() {
        return problem;
    }
}
