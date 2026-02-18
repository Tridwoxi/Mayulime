package infra.main;

import infra.io.Gui;
import infra.io.Logging;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import think.Manager;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.BadMapCodeException;
import think.repr.Problem.Feature;

/**
    Normal application launch point. Connects Gui (frontend) to Manager (backend).
 */
public final class App extends Application {

    private static final String NAME = "Mayulime";
    private static final String UNNAMED_PROBLEM_NAME = "Unnamed Problem";
    private static final double MIN_WIDTH_PX = 480.0;
    private static final double MIN_HEIGHT_PX = 320.0;
    private static final double DEFAULT_WIDTH_PX = 1280;
    private static final double DEFAULT_HEIGHT_PX = 720.0;

    private Manager manager;
    private Gui gui;

    @Override
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            exception.printStackTrace();
            Platform.exit();
        });
    }

    @Override
    public void start(final Stage primaryStage) {
        Logging.announcement("Launch point: Application");

        this.manager = new Manager(this::recieveSolution);
        this.gui = new Gui(this::recieveMapCode);

        primaryStage.setScene(gui);
        primaryStage.setTitle(NAME);
        primaryStage.setMinWidth(MIN_WIDTH_PX);
        primaryStage.setMinHeight(MIN_HEIGHT_PX);
        primaryStage.setWidth(DEFAULT_WIDTH_PX);
        primaryStage.setHeight(DEFAULT_HEIGHT_PX);
        primaryStage.show();
    }

    // == Connectors. =============================================================================

    private void recieveSolution(
        final String submitter,
        final Problem problem,
        final Grid<Feature> solution,
        final int score
    ) {
        if (gui == null || manager == null) {
            throw new IllegalStateException();
        }
        // PERF: Spamming the GUI with updates when each update invalidates all
        // previous updates is basically cyberbullying. Keep only the latest one.
        Platform.runLater(() -> gui.update(submitter, problem, solution, score));
    }

    private void recieveMapCode(final String mapCode) {
        if (gui == null || manager == null) {
            throw new IllegalStateException();
        }
        Problem problem = null;
        try {
            problem = new Problem(mapCode);
        } catch (BadMapCodeException exception) {
            Logging.warning("Bad MapCode; problem rejected");
        }
        if (problem != null) {
            manager.solve(problem);
            if (gui.getWindow() instanceof Stage stage) {
                final String problemName = problem.getName().isBlank()
                    ? UNNAMED_PROBLEM_NAME
                    : problem.getName();
                stage.setTitle("\"" + problemName + "\"");
            }
        }
    }
}
