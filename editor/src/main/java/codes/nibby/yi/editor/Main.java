package codes.nibby.yi.editor;

import codes.nibby.yi.editor.gui.board.GameBoard;
import codes.nibby.yi.editor.settings.Settings;
import codes.nibby.go.Annotation;
import codes.nibby.go.GoGameRules;
import codes.nibby.go.GoGameModel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Settings.load();
        GoGameModel game = new GoGameModel(19, 19, GoGameRules.CHINESE);
        game.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)
                .playMove(0, 1)
                .playMove(1, 1)
                .playMove(0, 2)
                .playMove(1, 2)
                .playMove(0, 3)
                .playMove(1, 3)
                .playMove(0, 4)
                .playMove(1, 4)
                .annotate(new Annotation.Triangle(0, 0))
                .annotate(new Annotation.Triangle(1, 0))
                .annotate(new Annotation.Square(0, 1))
                .annotate(new Annotation.Square(1, 1))
                .annotate(new Annotation.Circle(0, 2))
                .annotate(new Annotation.Circle(1, 2))
                .annotate(new Annotation.Cross(0, 3))
                .annotate(new Annotation.Cross(1, 3))
                .annotate(new Annotation.Fade(0, 4))
                .annotate(new Annotation.Fade(1, 4))
                .annotate(new Annotation.Line(0, 5, 1, 5))
                .annotate(new Annotation.Arrow(0, 6, 1, 6));

        GameBoard board = new GameBoard();
        board.setModel(game);
        board.setEditable(false);

        Scene scene = new Scene(board.getComponent(), 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
