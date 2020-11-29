package yi.editor;

import javafx.scene.control.Menu;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import yi.core.go.GameModel;
import yi.core.go.StandardGameRules;
import yi.editor.components.EditorPerspective;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.utilities.GameModelUtilities;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

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
        gameModel = GameModelUtilities.createGameModel(19, 19, StandardGameRules.CHINESE);
        frame = new EditorFrame(gameModel, EditorPerspective.COMPACT, this::initializeTestActions);
        stage = frame;
        stage.show();
        stage.requestFocus();
    }

    protected abstract void initializeTestActions(EditorActionManager manager);

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

    /**
     * Gets the {@link Menu} component in the {@link yi.editor.components.EditorMenuBar}
     * that corresponds to the given menu type. If it is not present, returns
     * {@link Optional#empty()}.
     *
     * @param menuType Menu type to retrieve menu for.
     * @return The corresponding menu, if it exists.
     */
    public Optional<Menu> getMenu(EditorMainMenuType menuType) {
        if (frame != null) {
            return frame.getMainMenuBar().getMenus().stream()
                    .filter(menu -> menu.getUserData() == menuType)
                    .findFirst();
        }
        return Optional.empty();
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
