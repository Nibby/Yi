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
 * Handles desktop file open events.
 */
public final class EditorOpenFileHandler {

    private EditorOpenFileHandler() {
        // Utility class, no instantiation
    }

    private static final Queue<File> OPEN_FILE_QUEUE = new ArrayDeque<>();
    private static boolean hasPreInitializationOpenFileEvent = false;

    public static void initialize() {
        Application.GetApplication().setEventHandler(new Application.EventHandler() {
            @Override
            public void handleOpenFilesAction(Application app, long time, String[] filePaths) {
                super.handleOpenFilesAction(app, time, filePaths);
                for (String filePath : filePaths) {
                    handleOpenFileRequest(new File(filePath));
                }
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

    private static void loadGameModel(File file) {
        try {
            GameModel gameModel = GameModelImporter.INSTANCE.fromFile(file.toPath());
            Platform.runLater(() -> {
                var window = new EditorWindow(gameModel);
                window.show();
            });
        } catch (GameParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAllQueuedOpenFiles() {
        while (!OPEN_FILE_QUEUE.isEmpty()) {
            File file = OPEN_FILE_QUEUE.poll();
            loadGameModel(file);
        }
    }

    public static boolean hasPreInitializationOpenFileEvent() {
        return hasPreInitializationOpenFileEvent;
    }
}
