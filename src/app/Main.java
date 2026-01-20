package app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import think.Solver;
import think.repr.Cell;
import think.repr.Problem;
import think.repr.Problem.InvalidSpecException;

/**
    Application launch point and communication hub.
 */
public final class Main extends Application {

    private static final String NAME = "Tridwoxi's Pathery AI";
    private static final AtomicReference<Main> INSTANCE = new AtomicReference<>();

    private Gui gui;

    @Override
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            exception.printStackTrace();
            Platform.exit();
        });
        final boolean success = INSTANCE.compareAndSet(null, this);
        assert success;
    }

    public static Main getInstance() {
        final Main stored = INSTANCE.get();
        assert stored != null;
        return stored;
    }

    @Override
    public void start(final Stage primaryStage) {
        this.gui = new Gui();
        primaryStage.setScene(gui);
        primaryStage.setTitle(NAME);
        primaryStage.setOnCloseRequest(event -> {
            Solver.getInstance().stop();
            Platform.exit();
        });
        primaryStage.show();
    }

    // == Connectors. ==================================================================

    public void send(final File file) {
        Problem problem = null;
        try {
            problem = new Problem(Files.readString(file.toPath()));
        } catch (InvalidSpecException exception) {
            System.err.println("Bad specification.");
        } catch (IOException exception) {
            System.err.println("Can't read file.");
        }
        if (problem != null) {
            Solver.getInstance().solve(problem);
            if (gui.getWindow() instanceof Stage stage) {
                stage.setTitle(file.getName());
            }
        }
    }

    public void recieve(
        final Class<? extends Runnable> strategyClass,
        final Problem problem,
        final HashSet<Cell> rubbers,
        final int score
    ) {
        Platform.runLater(() -> {
            gui.update(strategyClass, problem, rubbers, score);
            if (gui.getWindow() instanceof Stage stage) {
                stage.sizeToScene();
            }
        });
    }
}
