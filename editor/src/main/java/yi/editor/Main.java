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

        var gameModel = new GameModel(3, 3, GameRules.CHINESE);
        var editorFrame = new EditorFrame(gameModel);
        editorFrame.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
