package yi.editor;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import yi.core.go.*;
import yi.core.go.docformat.FileFormat;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.action.EditorActionHelper;
import yi.editor.framework.action.EditorAtomicAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

final class EditorStandardActions {

    private EditorStandardActions() {
    }

    protected static void initializeSharedActions() {
        createNewGameAction();
        createOpenGameAction();
        createSaveAction();
        createSaveAsAction();
    }

    private static void createNewGameAction() {
        Consumer<EditorActionHelper> action = helper -> {
            var frame = helper.getEditorFrame();
            // TODO: Show a new dialog prompting for new game document information.
            //       The values below are hard-coded, and are temporary.
            var existingModel = frame.getGameModel();
            var doIt = new AtomicBoolean(false);

            if (existingModel.isModified()) {
                var overwriteAlert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Would you like to save your changes to the current document?",
                        ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                overwriteAlert.showAndWait().ifPresent(selectedButton -> {
                    if (selectedButton == ButtonType.CANCEL) {
                        doIt.set(false);
                    } else if (selectedButton == ButtonType.YES) {
                        promptAndStoreSaveFile(existingModel, frame);
                        doIt.set(true);
                    } else {
                        doIt.set(true);
                    }
                });
            } else {
                doIt.set(true);
            }

            if (doIt.get()) {
                var newModel = new GameModel(19, 19, StandardGameRules.CHINESE);
                frame.setGameModel(newModel);
            }
        };
        new EditorAtomicAction(EditorTextResources.MENUITEM_NEW_GAME, action)
                .setInMainMenu(EditorMainMenuType.FILE, 0d)
                .setAccelerator(EditorAcceleratorId.NEW_GAME);
    }

    private static void createOpenGameAction() {
        Consumer<EditorActionHelper> action = helper -> {
            var frame = helper.getEditorFrame();
            var fileChooser = new FileChooser();
            fileChooser.setTitle(EditorTextResources.MENUITEM_OPEN_GAME.getLocalisedText());
            File selectedFile = fileChooser.showOpenDialog(frame);
            if (selectedFile != null) {
                try {
                    var importedModel = GameModelImporter.INSTANCE.fromFile(selectedFile.toPath());
                    frame.setGameModel(importedModel);
                } catch (GameParseException | IOException e) {
                    // TODO: Error handling
                    e.printStackTrace();
                }
            }
        };
        new EditorAtomicAction(EditorTextResources.MENUITEM_OPEN_GAME, action)
                .setInMainMenu(EditorMainMenuType.FILE, 0.001d)
                .setAccelerator(EditorAcceleratorId.OPEN_GAME);
    }

    private static void createSaveAction() {
        Consumer<EditorActionHelper> action = helper -> {
            var frame = helper.getEditorFrame();
            var existingModel = frame.getGameModel();
            Path saveFilePath = existingModel.getLastSavePath();

            if (saveFilePath == null) {
                saveFilePath = promptAndStoreSaveFile(existingModel, frame);
            }

            if (saveFilePath != null && existingModel.getLastSaveFormat() != null) {
                GameModelExporter.INSTANCE.toFile(existingModel, saveFilePath, existingModel.getLastSaveFormat());
            }
        };
        new EditorAtomicAction(EditorTextResources.MENUITEM_SAVE_GAME, action)
                .setInMainMenu(EditorMainMenuType.FILE, 0.002d)
                .setAccelerator(EditorAcceleratorId.SAVE_GAME);
    }

    private static void createSaveAsAction() {
        Consumer<EditorActionHelper> action = helper -> {
            var frame = helper.getEditorFrame();
            var existingModel = frame.getGameModel();
            Path saveFilePath = promptAndStoreSaveFile(existingModel, frame);
            FileFormat saveFileFormat = existingModel.getLastSaveFormat();

            if (saveFilePath != null) {
                GameModelExporter.INSTANCE.toFile(existingModel, saveFilePath, saveFileFormat);
            }
        };
        new EditorAtomicAction(EditorTextResources.MENUITEM_SAVE_AS_GAME, action)
                .setInMainMenu(EditorMainMenuType.FILE, 0d)
                .setAccelerator(EditorAcceleratorId.SAVE_AS_GAME);
    }

    /*
        TODO:
            1. Wrapper around FileFormat to provide an ExtensionFilter for FileChooser for
               save/load
            2. For now we're assuming the format being saved is SGF, but we need to determine
               this from the selected extension filter.
     */
    private static Path promptAndStoreSaveFile(GameModel gameModel, EditorFrame frame) {
        var saveFileChooser = new FileChooser();
        saveFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Smart Game Format (*.sgf)", "sgf"));
        saveFileChooser.setTitle("Select Save Location");
        File saveFile = saveFileChooser.showSaveDialog(frame);
        if (saveFile != null) {
            Path savePath = saveFile.toPath();
            gameModel.setLastSavePath(savePath);
            gameModel.setLastSaveFormat(FileFormat.SGF);
            return savePath;
        }
        return null;
    }
}
