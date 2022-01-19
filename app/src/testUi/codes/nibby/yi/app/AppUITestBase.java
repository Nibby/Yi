package codes.nibby.yi.app;

import codes.nibby.yi.app.framework.AppWindow;
import codes.nibby.yi.app.components.AppMainMenuType;
import codes.nibby.yi.app.components.AppMenuBar;
import codes.nibby.yi.app.components.AppPerspective;
import codes.nibby.yi.app.framework.global.GlobalHelper;
import codes.nibby.yi.app.framework.action.AppActionManager;
import codes.nibby.yi.app.utilities.GameModelUtilities;
import javafx.scene.control.Menu;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import codes.nibby.yi.models.GameModel;
import codes.nibby.yi.models.StandardGameRules;

import java.util.Optional;

@ExtendWith(ApplicationExtension.class)
public abstract class AppUITestBase {

    @BeforeAll
    public static void initializeTestEnvironment() {
        GlobalHelper.setRunningAsTest(true);
        GlobalHelper.setUseSystemMenuBar(false); // So that testFx can find the menu components...
    }

    protected AppWindow window;
    protected GameModel gameModel;

    @Start
    public void startFx(Stage stage) {
        GlobalHelper.initializeContext(null);

        gameModel = GameModelUtilities.createGameModel(19, 19, StandardGameRules.CHINESE);
        window = new AppWindow(gameModel, AppPerspective.COMPACT) {
            @Override
            protected void initializeActions(AppActionManager actionManager) {
                super.initializeActions(actionManager);
                initializeTestActions(actionManager);
            }
        };
        stage = window.getStage();
        stage.show();
        stage.requestFocus();
    }

    protected abstract void initializeTestActions(AppActionManager manager);

    @AfterEach
    public void dispose() {
        gameModel.dispose();
        System.gc();
    }

    public AppWindow getFrame() {
        return window;
    }

    public GameModel getGameModel() {
        return gameModel;
    }

    /**
     * Gets the {@link Menu} component in the {@link AppMenuBar}
     * that corresponds to the given menu type. If it is not present, returns
     * {@link Optional#empty()}.
     *
     * @param menuType Menu type to retrieve menu for.
     * @return The corresponding menu, if it exists.
     */
    public Optional<Menu> getMenu(AppMainMenuType menuType) {
        if (window != null) {
            return window.getMainMenuBar().getMenus().stream()
                    .filter(menu -> menu.getUserData() == menuType)
                    .findFirst();
        }
        return Optional.empty();
    }
}
