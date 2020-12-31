package yi.editor.framework;


import com.sun.glass.ui.Application;
import javafx.application.Platform;
import yi.core.go.GameModel;
import yi.core.go.GameModelImporter;
import yi.core.go.GameParseException;
import yi.editor.EditorWindow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Handles desktop application events.
 */
public final class EditorApplicationEventHandler {

    private EditorApplicationEventHandler() {
        // Utility class, no instantiation
    }

    private static final Queue<File> OPEN_FILE_QUEUE = new ArrayDeque<>();
    private static boolean hasPreInitializationOpenFileEvent = false;

    public static void initialize() {
        Application.EventHandler internalHandler = Application.GetApplication().getEventHandler();

        Application.GetApplication().setEventHandler(new Application.EventHandler() {
            @Override
            public void handleOpenFilesAction(Application app, long time, String[] filePaths) {
                for (String filePath : filePaths) {
                    handleOpenFileRequest(new File(filePath));
                }
            }

            @Override
            public void handleWillFinishLaunchingAction(Application app, long time) {
                internalHandler.handleWillFinishLaunchingAction(app, time);
            }

            @Override
            public void handleDidFinishLaunchingAction(Application app, long time) {
                internalHandler.handleDidFinishLaunchingAction(app, time);
            }

            @Override
            public void handleWillBecomeActiveAction(Application app, long time) {
                internalHandler.handleWillBecomeActiveAction(app, time);
            }

            @Override
            public void handleDidBecomeActiveAction(Application app, long time) {
                internalHandler.handleDidBecomeActiveAction(app, time);
            }

            @Override
            public void handleWillResignActiveAction(Application app, long time) {
                internalHandler.handleWillResignActiveAction(app, time);
            }

            @Override
            public void handleDidResignActiveAction(Application app, long time) {
                internalHandler.handleDidResignActiveAction(app, time);
            }

            @Override
            public void handleDidReceiveMemoryWarning(Application app, long time) {
                internalHandler.handleDidReceiveMemoryWarning(app, time);
            }

            @Override
            public void handleWillHideAction(Application app, long time) {
                internalHandler.handleWillHideAction(app, time);
            }

            @Override
            public void handleDidHideAction(Application app, long time) {
                internalHandler.handleDidHideAction(app, time);
            }

            @Override
            public void handleWillUnhideAction(Application app, long time) {
                internalHandler.handleWillUnhideAction(app, time);
            }

            @Override
            public void handleDidUnhideAction(Application app, long time) {
                internalHandler.handleDidUnhideAction(app, time);
            }

            @Override
            public void handleQuitAction(Application app, long time) {
                internalHandler.handleQuitAction(app, time);
            }

            @Override
            public boolean handleThemeChanged(String themeName) {
                return internalHandler.handleThemeChanged(themeName);
            }
        });
    }

    private static void handleOpenFileRequest(File file) {
        if (EditorHelper.isInitialized()) {
            loadGameModel(file);
        } else {
            queueFile(file);
        }
    }

    private static void queueFile(File file) {
        hasPreInitializationOpenFileEvent = true;
        // Process this in the Fx thread once the application has finished initializing.
        OPEN_FILE_QUEUE.add(file);
    }

    private static boolean loadGameModel(File file) {
        boolean loadedSomething = false;
        try {
            GameModel gameModel = GameModelImporter.INSTANCE.fromFile(file.toPath());
            loadedSomething = true;
            Platform.runLater(() -> {
                var window = new EditorWindow(gameModel);
                window.show();
            });
        } catch (GameParseException | IOException e) {
            e.printStackTrace();
        }
        return loadedSomething;
    }

    public static void loadAllQueuedOpenFiles() {
        boolean loadedSomething = false;
        while (!OPEN_FILE_QUEUE.isEmpty()) {
            File file = OPEN_FILE_QUEUE.poll();
            loadedSomething |= loadGameModel(file);
        }

        hasPreInitializationOpenFileEvent = loadedSomething;
    }

    public static boolean hasPreInitializationOpenFileEvent() {
        return hasPreInitializationOpenFileEvent;
    }
}
