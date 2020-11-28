package yi.editor;


import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import yi.component.FontManager;
import yi.component.SkinManager;
import yi.editor.framework.accelerator.EditorAcceleratorManager;
import yi.editor.settings.EditorSettings;
import yi.editor.utilities.GameModelUtilities;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        EditorHelper.initializeContext();

        var gameModel = GameModelUtilities.createGameModel();
        var editorFrame = new EditorFrame(gameModel, EditorSettings.general.getCurrentLayout());
        editorFrame.show();
    }
}
