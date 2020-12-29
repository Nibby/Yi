package yi.editor;

import javafx.application.Application;
import javafx.stage.Stage;
import yi.editor.framework.EditorHelper;
import yi.editor.framework.EditorApplicationEventHandler;
import yi.editor.settings.EditorSettings;
import yi.editor.utilities.GameModelUtilities;

public class EditorFxMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        EditorHelper.initializeContext();

        if (!EditorApplicationEventHandler.hasPreInitializationOpenFileEvent()) {
            var gameModel = GameModelUtilities.createGameModel();
            var window = new EditorWindow(gameModel, EditorSettings.general.getPerspective());
            window.show();
        }
    }

}
