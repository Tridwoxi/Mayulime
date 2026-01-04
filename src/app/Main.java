package app;

import java.io.File;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main extends Application {

    private Gui gui;

    @Override
    public void start(final Stage primaryStage) {
        gui = new Gui();
        primaryStage.setTitle("henlo");
        primaryStage.setScene(new Scene(gui));
        primaryStage.show();
    }

    public static void solveProblem(File file) {
        throw new UnsupportedOperationException();
    }
}
