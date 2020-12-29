package yi.editor.framework;


import javafx.application.Platform;
import yi.core.go.GameModel;
import yi.core.go.GameModelImporter;
import yi.core.go.GameParseException;
import yi.editor.EditorWindow;

import javax.swing.*;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
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
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)) {
            Desktop.getDesktop().setOpenFileHandler(handler -> {
                List<File> files = handler.getFiles();
                for (File file : files) {
                    handleOpenFileRequest(file);
                }
            });
        }
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
