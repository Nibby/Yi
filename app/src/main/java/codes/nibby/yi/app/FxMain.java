package codes.nibby.yi.app;

import codes.nibby.yi.app.framework.GlobalApplicationEventHandler;
import codes.nibby.yi.app.framework.GlobalHelper;
import codes.nibby.yi.app.framework.AppWindow;
import codes.nibby.yi.app.settings.AppSettings;
import codes.nibby.yi.app.utilities.GameModelUtilities;
import javafx.application.Application;
import javafx.stage.Stage;

public class FxMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        GlobalHelper.initializeContext(getParameters());

        if (!GlobalApplicationEventHandler.hasPreInitializationOpenFileEvent()) {
            var gameModel = GameModelUtilities.createGameModel();
            var window = new AppWindow(gameModel, AppSettings.general.getPerspective());
            window.show();
        }
    }

}
