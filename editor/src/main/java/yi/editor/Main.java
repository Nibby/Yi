package yi.editor;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import yi.component.board.GameBoardViewer;
import yi.component.gametree.GameTreeViewer;
import yi.core.go.GameModel;
import yi.core.go.GameRules;
import yi.editor.settings.Settings;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Settings.load();
        GameModel game = new GameModel(19, 19, GameRules.CHINESE);
//        game.beginMoveSequence()
//                .playMove(0, 0)
//                .playMove(1, 0)
//                .playMove(0, 1)
//                .playMove(1, 1)
//                .playMove(0, 2)
//                .playMove(1, 2)
//                .playMove(0, 3)
//                .playMove(1, 3)
//                .playMove(0, 4)
//                .playMove(1, 4)
//                .annotate(new GoAnnotation.Triangle(0, 0))
//                .annotate(new GoAnnotation.Triangle(1, 0))
//                .annotate(new GoAnnotation.Square(0, 1))
//                .annotate(new GoAnnotation.Square(1, 1))
//                .annotate(new GoAnnotation.Circle(0, 2))
//                .annotate(new GoAnnotation.Circle(1, 2))
//                .annotate(new GoAnnotation.Cross(0, 3))
//                .annotate(new GoAnnotation.Cross(1, 3))
//                .annotate(new GoAnnotation.Fade(0, 4))
//                .annotate(new GoAnnotation.Fade(1, 4))
//                .annotate(new GoAnnotation.Line(0, 5, 1, 5))
//                .annotate(new GoAnnotation.Arrow(0, 6, 1, 6));
//
//        game.toPreviousMove();
//        game.toPreviousMove();
//        game.playMove(5, 5);
//        game.playMove(5, 6);
//        game.playMove(5, 7);
//        game.playMove(5, 8);
//        game.toPreviousMove();
//        game.toPreviousMove();
//        game.playMove(3,4);
//        game.playMove(3,5);
//        game.playMove(3,6);
//        GoMoveSubmitResult goMoveSubmitResult = game.playMove(5, 9);
//        game.setCurrentMove(goMoveSubmitResult.getMoveNode());

        GameBoardViewer board = new GameBoardViewer(Settings.getBoardSettings());
        board.setGameModel(game);
//        board.setEditable(false);

        GameTreeViewer tree = new GameTreeViewer(game);

        SplitPane content = new SplitPane(board.getComponent(), tree.getComponent());
        content.setDividerPosition(0, 0.7d);

        Scene scene = new Scene(content, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
