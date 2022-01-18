package codes.nibby.yi.app.framework;


import com.sun.glass.ui.Application;
import javafx.application.Platform;
import org.jetbrains.annotations.Nullable;
import codes.nibby.yi.models.GameModel;
import codes.nibby.yi.models.GameModelImporter;
import codes.nibby.yi.models.GameParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Handles desktop application events.
 */
public final class GlobalApplicationEventHandler {

    private GlobalApplicationEventHandler() {
        // Utility class, no instantiation
    }

    private static final Queue<File> OPEN_FILE_QUEUE = new ArrayDeque<>();
    private static boolean hasPreInitializationOpenFileEvent = false;

    public static void initialize(@Nullable javafx.application.Application.Parameters parameters) {
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

        handlePendingOpenFilesInProgramArgs(parameters);
    }

    /*
        When a file is opened whose extension is associated with this application, the absolute file path is appended
        to the program launch parameters on non-macOS platforms.
     */
    private static void handlePendingOpenFilesInProgramArgs(@Nullable javafx.application.Application.Parameters parameters) {
        if (parameters == null) {
            return;
        }
        List<String> paramValues = parameters.getRaw();
        for (String param : paramValues) {
            try {
                Path paramAsPath = Paths.get(param);
                handleOpenFileRequest(paramAsPath.toFile());
            } catch (InvalidPathException e) {
                // Oh well, this parameter is not an absolute file path, ignore it.
            }
        }
    }

    private static void handleOpenFileRequest(File file) {
        if (file.exists() && file.getAbsolutePath().contains(File.separator)) {
            if (GlobalHelper.isInitialized()) {
                loadGameModel(file);
            } else {
                queueFile(file);
            }
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
                var window = new AppWindow(gameModel);
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
