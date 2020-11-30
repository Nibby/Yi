package yi.editor;


import javafx.application.Application;
import javafx.stage.Stage;
import yi.editor.settings.EditorSettings;
import yi.editor.utilities.GameModelUtilities;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        EditorHelper.initializeContext();

        var gameModel = GameModelUtilities.createGameModel();
        var editorFrame = new EditorWindow(gameModel, EditorSettings.general.getPerspective());
        editorFrame.show();
    }
}
