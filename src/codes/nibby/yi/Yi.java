package codes.nibby.yi;

import codes.nibby.yi.gui.board.GameBoard;
import codes.nibby.yi.model.GoGame;
import codes.nibby.yi.settings.Settings;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Yi extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Settings.load();

        GoGame game = new GoGame(19, 19);

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
