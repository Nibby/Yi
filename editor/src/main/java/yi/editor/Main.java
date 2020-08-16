package yi.editor;


import javafx.application.Application;
import javafx.stage.Stage;
import yi.component.SkinManager;
import yi.core.go.GameModel;
import yi.core.go.GameModelImporter;
import yi.core.go.GameRules;
import yi.editor.settings.Settings;

import java.nio.file.Paths;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        SkinManager.useDefaultSkin();
        Settings.load();

        var gameModel = GameModelImporter.INSTANCE.fromFile(Paths.get(System.getProperty("user.home")).resolve("Downloads").resolve("blink.sgf"));
//        var gameModel = new GameModel(19, 19, GameRules.CHINESE);
        var editorFrame = new EditorFrame(gameModel);
        editorFrame.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
