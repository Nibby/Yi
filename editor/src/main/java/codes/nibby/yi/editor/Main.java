package codes.nibby.yi.editor;

import codes.nibby.yi.common.GoGame;
import codes.nibby.yi.common.ruleset.Ruleset;
import codes.nibby.yi.editor.gui.board.GameBoard;
import codes.nibby.yi.editor.settings.Settings;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Settings.load();
        GoGame game = new GoGame(19, 19, Ruleset.CHINESE);

        GameBoard board = new GameBoard();
        board.initialize(game);

        Scene scene = new Scene(board.getComponent(), 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
