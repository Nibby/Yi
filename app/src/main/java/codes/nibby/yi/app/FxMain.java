package codes.nibby.yi.app;

import codes.nibby.yi.app.framework.global.GlobalApplicationEventHandler;
import codes.nibby.yi.app.framework.global.GlobalHelper;
import codes.nibby.yi.app.framework.AppWindow;
import codes.nibby.yi.app.settings.AppSettings;
import codes.nibby.yi.app.utilities.GameModelUtilities;
import javafx.application.Application;
import javafx.stage.Stage;

public class FxMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        initialize();

        if (!GlobalHelper.isRunningAsTest()) {
            openPendingDocuments();
        }

        if (!GlobalApplicationEventHandler.hasPreInitializationOpenFileEvent()) {
            newGameRecordWindow();
        }
    }

    private void initialize() {
        GlobalHelper.initializeContext(getParameters());
    }

    private void openPendingDocuments() {
        GlobalApplicationEventHandler.loadAllQueuedOpenFiles();
    }

    private void newGameRecordWindow() {
        var gameModel = GameModelUtilities.createGameModel();
        var window = new AppWindow(gameModel, AppSettings.general.getPerspective());
        window.show();
    }
}
