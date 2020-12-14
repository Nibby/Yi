package yi.editor;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import yi.editor.framework.EditorComponent;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionContext;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.framework.action.EditorBasicAction;
import yi.models.go.*;
import yi.models.go.docformat.FileFormat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

final class EditorStandardActions implements EditorComponent<Object> {

    private static final EditorStandardActions INSTANCE = new EditorStandardActions();

    private final Set<EditorAction> standardActions = new HashSet<>();

    private EditorStandardActions() {
        createNewGameAction();
        createOpenGameAction();
        createSaveAction();
        createSaveAsAction();
    }

    protected static EditorStandardActions getInstance() {
        return INSTANCE;
    }

    private void createNewGameAction() {
        Consumer<EditorActionContext> action = helper -> {
            var window = helper.getEditorWindow();
            // TODO: Show a new dialog prompting for new game document information.
            //       The values below are hard-coded, and are temporary.
            var existingModel = window.getGameModel();
            var doIt = new AtomicBoolean(false);

            if (existingModel.isModified()) {
                var overwriteAlert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Would you like to save your changes to the current document?",
                        ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                overwriteAlert.showAndWait().ifPresent(selectedButton -> {
                    if (selectedButton == ButtonType.CANCEL) {
                        doIt.set(false);
                    } else if (selectedButton == ButtonType.YES) {
                        promptAndStoreSaveFile(existingModel, window);
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
                window.setGameModel(newModel);
            }
        };

        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_NEW_GAME, action);
        actionItem.setInMainMenu(EditorMainMenuType.FILE, 0d);
        actionItem.setAccelerator(EditorAcceleratorId.NEW_GAME);

        standardActions.add(actionItem);
    }

    private void createOpenGameAction() {
        Consumer<EditorActionContext> action = helper -> {
            var window = helper.getEditorWindow();
            var fileChooser = new FileChooser();
            fileChooser.setTitle(EditorTextResources.MENUITEM_OPEN_GAME.getLocalisedText());
            File selectedFile = fileChooser.showOpenDialog(window);
            if (selectedFile != null) {
                try {
                    var importedModel = GameModelImporter.INSTANCE.fromFile(selectedFile.toPath());
                    window.setGameModel(importedModel);
                } catch (GameParseException | IOException e) {
                    // TODO: Error handling
                    e.printStackTrace();
                }
            }
        };

        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_OPEN_GAME, action);
        actionItem.setInMainMenu(EditorMainMenuType.FILE, 0.001d);
        actionItem.setAccelerator(EditorAcceleratorId.OPEN_GAME);

        standardActions.add(actionItem);
    }

    private void createSaveAction() {
        Consumer<EditorActionContext> action = helper -> {
            var window = helper.getEditorWindow();
            var existingModel = window.getGameModel();
            Path saveFilePath = existingModel.getLastSavePath();

            if (saveFilePath == null) {
                saveFilePath = promptAndStoreSaveFile(existingModel, window);
            }

            if (saveFilePath != null && existingModel.getLastSaveFormat() != null) {
                GameModelExporter.INSTANCE.toFile(existingModel, saveFilePath, existingModel.getLastSaveFormat());
            }
        };

        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_SAVE_GAME, action);
        actionItem.setInMainMenu(EditorMainMenuType.FILE, 0.002d);
        actionItem.setAccelerator(EditorAcceleratorId.SAVE_GAME);

        standardActions.add(actionItem);
    }

    private void createSaveAsAction() {
        Consumer<EditorActionContext> action = helper -> {
            var window = helper.getEditorWindow();
            var existingModel = window.getGameModel();
            Path saveFilePath = promptAndStoreSaveFile(existingModel, window);
            FileFormat saveFileFormat = existingModel.getLastSaveFormat();

            if (saveFilePath != null) {
                GameModelExporter.INSTANCE.toFile(existingModel, saveFilePath, saveFileFormat);
            }
        };

        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_SAVE_AS_GAME, action);
        actionItem.setInMainMenu(EditorMainMenuType.FILE, 0.003d);
        actionItem.setAccelerator(EditorAcceleratorId.SAVE_AS_GAME);

        standardActions.add(actionItem);
    }

    /*
        TODO:
            1. Wrapper around FileFormat to provide an ExtensionFilter for FileChooser for
               save/load
            2. For now we're assuming the format being saved is SGF, but we need to determine
               this from the selected extension filter.
     */
    private static Path promptAndStoreSaveFile(GameModel gameModel, EditorWindow window) {
        var saveFileChooser = new FileChooser();
        saveFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Smart Game Format (*.sgf)", "sgf"));
        saveFileChooser.setTitle("Select Save Location");
        File saveFile = saveFileChooser.showSaveDialog(window);
        if (saveFile != null) {
            Path savePath = saveFile.toPath();
            gameModel.setLastSavePath(savePath);
            gameModel.setLastSaveFormat(FileFormat.SGF);
            return savePath;
        }
        return null;
    }

    @Override
    public EditorAction[] getActions(EditorActionManager actionManager) {
        return standardActions.toArray(new EditorAction[0]);
    }

    @Override
    public Optional<Object> getComponent() {
        return Optional.empty();
    }
}
