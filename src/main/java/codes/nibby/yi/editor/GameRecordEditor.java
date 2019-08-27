package codes.nibby.yi.editor;

import codes.nibby.yi.Yi;
import codes.nibby.yi.board.GameBoard;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.rules.GameRules;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GameRecordEditor extends Stage {

    private EditorBoardController controller;

    public GameRecordEditor() {
        controller = new EditorBoardController();

        Game game = new Game(GameRules.CHINESE, 19, 19);
        GameBoard board = new GameBoard(game, controller);
        BorderPane root = new BorderPane(board);
        Scene scene = new Scene(root, 800, 600);
        setScene(scene);
        setTitle(Yi.TITLE);
    }

}
