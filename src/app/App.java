package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import think.Manager;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.BadMapCodeException;
import think.repr.Problem.Feature;
import think.tools.Logging;

/**
    Application launch point. Connects Gui (frontend) to Manager (backend).
 */
public final class App extends Application {

    private static final String NAME = "Mayulime";
    private static final String UNNAMED_PROBLEM_NAME = "Unnamed Problem";
    private static final double MIN_WIDTH_PX = 480.0;
    private static final double MIN_HEIGHT_PX = 320.0;

    private Manager manager;
    private Gui gui;
    private Problem current;

    @Override
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            exception.printStackTrace();
            Platform.exit();
        });
    }

    @Override
    public void start(final Stage primaryStage) {
        assert Test.runAllTests();
        this.manager = new Manager(this::recieveSolution);
        this.gui = new Gui(this::recieveMapCode);
        this.current = null;

        primaryStage.setScene(gui);
        primaryStage.setTitle(NAME);
        primaryStage.setMinWidth(MIN_WIDTH_PX);
        primaryStage.setMinHeight(MIN_HEIGHT_PX);
        primaryStage.setWidth(MIN_WIDTH_PX);
        primaryStage.setHeight(MIN_HEIGHT_PX);
        primaryStage.show();
    }

    // == Connectors. ==================================================================

    /**
        Recieve a Solution from somewhere (perhaps the Manager) and pass it to the GUI.
     */
    private void recieveSolution(
        final String submitter,
        final Problem problem,
        final Grid<Feature> solution,
        final int score
    ) {
        assert gui != null && manager != null;
        // PERF: Spamming the GUI with updates when each update invalidates all
        // previous updates is basically cyberbullying. Keep only the latest one.
        Platform.runLater(() -> {
            gui.update(submitter, problem, solution, score);
            if (problem == current) {
                return;
            }
            current = problem;
            if (gui.getWindow() instanceof Stage stage) {
                stage.sizeToScene();
            }
        });
    }

    /**
        Recieve a MapCode from somewhere (perhaps the GUI) and pass it to the Manager.
     */
    private void recieveMapCode(final String mapCode) {
        assert gui != null && manager != null;
        Problem problem = null;
        try {
            problem = new Problem(mapCode);
        } catch (BadMapCodeException exception) {
            Logging.log(getClass(), "Bad MapCode; problem rejected");
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
