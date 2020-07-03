package codes.nibby.yi.editor;

import codes.nibby.yi.editor.gui.board.GameBoard;
import codes.nibby.yi.editor.settings.Settings;
import codes.nibby.yi.go.GoGameRules;
import codes.nibby.yi.go.GoGameModel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Settings.load();
        GoGameModel game = new GoGameModel(13, 13, GoGameRules.CHINESE);

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
