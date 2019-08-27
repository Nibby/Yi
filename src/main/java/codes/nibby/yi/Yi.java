package codes.nibby.yi;

import codes.nibby.yi.board.GameBoard;
import codes.nibby.yi.game.Game;
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
    public static final String VERSION = "v0.0.6";
    public static final String TITLE = NAME + " - " + VERSION;
    public static final String CHARSET = "UTF-8";

    public static final String PATH_CONFIG = "config.json";


    @Override
    public void start(Stage primaryStage) throws Exception {
        System.setProperty("file.encoding", CHARSET);

        Game game = new Game();
        game.setBoardSize(19, 19);
        TestBoardController controller = new TestBoardController();
        GameBoard board = new GameBoard(game, controller);
        BorderPane root = new BorderPane(board);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle(TITLE);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void exit() {
        System.exit(0);
    }

}
