package yi.editor;

import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import yi.core.go.GameModel;
import yi.core.go.StandardGameRules;
import yi.editor.components.EditorPerspective;
import yi.editor.utilities.GameModelUtilities;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ExtendWith(ApplicationExtension.class)
public abstract class EditorUITestBase {

    @BeforeAll
    public static void initializeContext() {
        EditorHelper.setRunningAsTest(true);
        EditorHelper.setUseSystemMenuBar(false); // So that testFx can find the menu components...
        EditorHelper.setPreferredSettingsRootPath(getSettingsPathForTests());
        EditorHelper.initializeContext();
    }

    protected EditorFrame frame;
    protected GameModel gameModel;

    @Start
    public void startFx(Stage stage) {
        performTasksBeforeCreatingFrame();
        
        gameModel = GameModelUtilities.createGameModel(19, 19, StandardGameRules.CHINESE);
        frame = new EditorFrame(gameModel, EditorPerspective.COMPACT);
        stage = frame;
        stage.show();
        stage.requestFocus();
    }

    protected abstract void performTasksBeforeCreatingFrame();

    @AfterEach
    public void dispose() {
        gameModel.dispose();
        System.gc();
    }

    public EditorFrame getFrame() {
        return frame;
    }

    public GameModel getGameModel() {
        return gameModel;
    }

    private static Path getSettingsPathForTests() {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        if (currentDir.getFileName().endsWith("editor")) {
            currentDir = currentDir.getParent();
        }

        Path tempDir = currentDir.resolve("temp");
        if (Files.exists(tempDir) && Files.isDirectory(tempDir)) {
            return tempDir;
        }

        throw new IllegalStateException("Cannot set root path for tests. currentDir: " + currentDir);
    }

}
