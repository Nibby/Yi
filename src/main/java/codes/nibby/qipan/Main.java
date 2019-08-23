package codes.nibby.qipan;

import codes.nibby.qipan.board.GameBoard;
import codes.nibby.qipan.game.Game;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Entry point to the application.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class Main extends Application {

    public static final String NAME = "QiPan";
    public static final String VERSION = "v0.0.1";
    public static final String TITLE = NAME + " " + VERSION;
    public static final String CHARSET = "UTF-8";

    public static final String PATH_CONFIG = "config.json";

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.setProperty("file.encoding", CHARSET);

        Game game = new Game();
        game.setBoardSize(19, 19);
        GameBoard board = new GameBoard(game, null);
        BorderPane root = new BorderPane(board);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
