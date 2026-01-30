package app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import think.Manager;
import think.Manager.Strategy;
import think.repr.Cell;
import think.repr.Problem;
import think.repr.Problem.BadMapCodeException;

/**
    Application launch point. Connects Gui (frontend) to Manager (backend).
 */
public final class Main extends Application {

    private static final String NAME = "Tridwoxi's Pathery AI";
    private static final double MIN_WIDTH_PX = 480.0;
    private static final double MIN_HEIGHT_PX = 320.0;
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
        primaryStage.setMinWidth(MIN_WIDTH_PX);
        primaryStage.setMinHeight(MIN_HEIGHT_PX);
        primaryStage.setWidth(MIN_WIDTH_PX);
        primaryStage.setHeight(MIN_HEIGHT_PX);
        primaryStage.setOnCloseRequest(event -> {
            Manager.getInstance().stop();
            Platform.exit();
        });
        primaryStage.show();
    }

    // == Connectors. ==================================================================

    public void send(final File file) {
        Problem problem = null;
        try {
            problem = new Problem(Files.readString(file.toPath()));
        } catch (BadMapCodeException exception) {
            System.err.println("Bad MapCode.");
        } catch (IOException exception) {
            System.err.println("Can't read file.");
        }
        if (problem != null) {
            Manager.getInstance().solve(problem);
            if (gui.getWindow() instanceof Stage stage) {
                stage.setTitle(file.getName());
            }
        }
    }

    public void receive(
        final Strategy submitter,
        final Problem problem,
        final HashSet<Cell> playerWalls,
        final int score
    ) {
        Platform.runLater(() -> {
            gui.update(submitter, problem, playerWalls, score);
            if (gui.getWindow() instanceof Stage stage) {
                stage.sizeToScene();
            }
        });
    }
}
