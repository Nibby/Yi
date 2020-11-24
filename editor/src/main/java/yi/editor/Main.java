package yi.editor;


import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import yi.component.FontManager;
import yi.component.SkinManager;
import yi.core.go.GameModel;
import yi.core.go.GameRules;
import yi.editor.settings.Settings;

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
        loadBundledFonts();
        FontManager.setDefaultFont(new Font("Noto Sans", 12d));
        SkinManager.useDefaultSkin();
        Settings.load();

        var gameModel = new GameModel(19, 19, GameRules.CHINESE);
        var editorFrame = new EditorFrame(gameModel);
        editorFrame.show();
    }

    private void loadBundledFonts() {
        final String FONT_RESOURCE_DIR = "/fonts/";
        URI fontDirectoryUri;

        try {
            fontDirectoryUri = Main.class.getResource(FONT_RESOURCE_DIR).toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Malformed font resource directory value: " +
                    "\"" + FONT_RESOURCE_DIR + "\"");
        }

        var fontDirectoryAsPath = Paths.get(fontDirectoryUri);
        try {
            FontManager.loadFontsInDirectory(fontDirectoryAsPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
