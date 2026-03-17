package unit;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import think.domain.codec.Parser;
import think.domain.codec.Parser.BadMapCodeException;
import think.domain.model.Puzzle;
import think.manager.Manager;
import think.manager.SolverRegistry;

public final class ManagerLifecycle {

    private static final String SIMPLE_MAPCODE = """
        3.3.2.Copy state...:,s1.,f1.
        """;

    @Test
    public void stopWaitsForInFlightProposalDelivery() throws Exception {
        final Puzzle puzzle = Parser.parse(SIMPLE_MAPCODE);
        final CountDownLatch listenerStarted = new CountDownLatch(1);
        final CountDownLatch releaseListener = new CountDownLatch(1);
        final Manager manager = new Manager(
            proposal -> {
                listenerStarted.countDown();
                await(releaseListener);
            },
            List.of(SolverRegistry.BASELINE)
        );

        manager.solve(puzzle);
        Assertions.assertTrue(listenerStarted.await(1, TimeUnit.SECONDS));

        final ExecutorService stopExecutor = Executors.newSingleThreadExecutor();
        try {
            final Future<?> stopFuture = stopExecutor.submit(manager::stop);

            Assertions.assertThrows(TimeoutException.class, () -> stopFuture.get(50, TimeUnit.MILLISECONDS));

            releaseListener.countDown();
            stopFuture.get(1, TimeUnit.SECONDS);
        } finally {
            releaseListener.countDown();
            stopExecutor.shutdownNow();
        }
    }

    private static void await(final CountDownLatch latch) {
        boolean interrupted = false;
        while (true) {
            try {
                latch.await();
                break;
            } catch (final InterruptedException exception) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
