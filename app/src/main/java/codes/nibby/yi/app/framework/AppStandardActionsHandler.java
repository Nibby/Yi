package codes.nibby.yi.app.framework;

import codes.nibby.yi.app.components.AppPerspective;
import codes.nibby.yi.app.dialogs.GameModelEditDialog;
import codes.nibby.yi.app.framework.action.AppActionContext;
import codes.nibby.yi.app.utilities.GameModelUtilities;
import codes.nibby.yi.models.GameModel;
import codes.nibby.yi.models.GameModelExporter;
import codes.nibby.yi.models.GameModelImporter;
import codes.nibby.yi.models.GameParseException;
import javafx.stage.FileChooser;
import codes.nibby.yi.app.framework.modal.ModalActionButton;
import codes.nibby.yi.app.framework.modal.YiModalAlertPane;
import codes.nibby.yi.models.docformat.FileFormat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

final class AppStandardActionsHandler implements AppStandardActions.ActionHandler {

    AppStandardActionsHandler() {
        // Make inaccessible to exported API
    }

    @Override
    public void handleCreateNewGame(AppActionContext context) {
        var window = context.getInvokerWindow();
        // TODO: Show a new dialog prompting for new game document information.
        //       The values below are hard-coded, and are temporary.
        var existingModel = window.getGameModel();
        Runnable createGameTask = () -> {
            var newGameDialog = new GameModelEditDialog();
            newGameDialog.setCloseCallback(button -> {
                if (button == newGameDialog.actionButton) {
                    GameModel newModel = newGameDialog.createNewGameModel();
                    window.setGameModel(newModel);
                }
                return true;
            });
            window.pushModalContent(newGameDialog);
        };

        if (existingModel.isModified()) {
            var saveButton = ModalActionButton.createPrimaryButton(AppText.SAVE, null);
            var dontSaveButton = ModalActionButton.createSecondaryButton(AppText.DONT_SAVE, null);
            var cancelButton = ModalActionButton.createSecondaryButton(AppText.CANCEL, null);

            var overwriteAlert = new YiModalAlertPane(
                "Create new document",
                "Would you like to save your changes to the current document?"
            );
            overwriteAlert.setActionButtons(saveButton, cancelButton, dontSaveButton);
            overwriteAlert.setDefaultControlButton(saveButton);
            overwriteAlert.setCloseCallback(button -> {
                boolean createNewGame = false;

                if (saveButton.equals(button)) {
                    Path savePath = promptAndStoreSaveFile(existingModel, window);
                    if (savePath == null) {
                        return false; // user interrupted from file selection
                    }
                    createNewGame = true;
                } else if (dontSaveButton.equals(button)) {
                    createNewGame = true;
                }

                if (createNewGame) {
                    createGameTask.run();
                }
                return true;
            });
            window.pushModalContent(overwriteAlert);
        } else {
            createGameTask.run();
        }
    }

    @Override
    public void handleCreateGameInNewWindow(AppActionContext context) {
        var newWindow = new AppWindow(GameModelUtilities.createGameModel(), AppPerspective.EDIT);
        newWindow.show();
    }

    @Override
    public void handleOpenGame(AppActionContext context) {
        var window = context.getInvokerWindow();
        var fileChooser = new FileChooser();
        fileChooser.setTitle(AppText.MENUITEM_OPEN_GAME.getLocalisedText());
        File selectedFile = fileChooser.showOpenDialog(window.getStage());
        if (selectedFile != null) {
            try {
                var importedModel = GameModelImporter.INSTANCE.fromFile(selectedFile.toPath());
                window.setGameModel(importedModel);
            } catch (GameParseException | IOException e) {
                // TODO: Error handling
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleSaveGame(AppActionContext context) {
        var window = context.getInvokerWindow();
        var existingModel = window.getGameModel();
        Path saveFilePath = existingModel.getLastSavePath();

        if (saveFilePath == null) {
            saveFilePath = promptAndStoreSaveFile(existingModel, window);
        }

        if (saveFilePath != null && existingModel.getLastSaveFormat() != null) {
            GameModelExporter.INSTANCE.toFile(existingModel, saveFilePath, existingModel.getLastSaveFormat());
        }
    }

    @Override
    public void handleSaveAsGame(AppActionContext context) {
        var window = context.getInvokerWindow();
        var existingModel = window.getGameModel();
        Path saveFilePath = promptAndStoreSaveFile(existingModel, window);
        FileFormat saveFileFormat = existingModel.getLastSaveFormat();

        if (saveFilePath != null) {
            GameModelExporter.INSTANCE.toFile(existingModel, saveFilePath, saveFileFormat);
        }
    }

    /*
        TODO:
            1. Wrapper around FileFormat to provide an ExtensionFilter for FileChooser for
               save/load
            2. For now we're assuming the format being saved is SGF, but we need to determine
               this from the selected extension filter.
     */
    private static Path promptAndStoreSaveFile(GameModel gameModel, AppWindow window) {
        var saveFileChooser = new FileChooser();
        saveFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Smart Game Format (*.sgf)", "sgf"));
        saveFileChooser.setTitle("Select Save Location");
        File saveFile = saveFileChooser.showSaveDialog(window.getStage());
        if (saveFile != null) {
            Path savePath = saveFile.toPath();
            gameModel.setLastSavePath(savePath);
            gameModel.setLastSaveFormat(FileFormat.SGF);
            return savePath;
        }
        return null;
    }

}
