package codes.nibby.yi;

import codes.nibby.yi.board.GameBoard;
import codes.nibby.yi.editor.GameRecordEditor;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.rules.ChineseGameRules;
import codes.nibby.yi.game.rules.GameRules;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
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
    public static final String VERSION = "v0.1.0";
    public static final String TITLE = NAME + " - " + VERSION;
    public static final String CHARSET = "UTF-8";

    public static final String PATH_CONFIG = "config.json";


    @Override
    public void start(Stage primaryStage) throws Exception {
        System.setProperty("file.encoding", CHARSET);

        GameRecordEditor editor = new GameRecordEditor();
        editor.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void exit() {
        System.exit(0);
    }

}
