package codes.nibby.yi;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.editor.GameEditorWindow;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application class.
 *
 *
 * @author Kevin Yang
 * Created on 24 August 2019
 */
public class Yi extends Application {

    public static final String NAME = "Yi";
    public static final String VERSION = "v0.0.8";
    public static final String TITLE = NAME + " - " + VERSION;
    public static final String CHARSET = "UTF-16";

    public static final String PATH_CONFIG = "config.json";

    @Override
    public void start(Stage primaryStage) {
        System.setProperty("file.encoding", CHARSET);
        GameEditorWindow editor = new GameEditorWindow();
        editor.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void exit() {
        System.exit(0);
    }

}
