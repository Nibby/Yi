package yi.editor.components;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import yi.component.YiMenu;
import yi.component.YiMenuItem;
import yi.component.i18n.I18n;
import yi.component.i18n.Language;
import yi.component.utilities.GuiUtilities;
import yi.component.utilities.KeyModifier;
import yi.core.go.*;
import yi.core.go.docformat.FileFormat;
import yi.editor.EditorFrame;
import yi.editor.Yi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static yi.editor.Translations.Menu.*;

/**
 * Primary menu bar for {@link yi.editor.EditorFrame}.
 */
// TODO: Extract labels into resource keys for internationalization
// TODO: This class is one big work in progress
public class EditorMenuBar extends MenuBar {

    public EditorMenuBar(EditorFrame frame) {
        var fileMenu = createFileMenu(frame);
        var editMenu = new YiMenu(MENU_EDIT);
        var toolsMenu = new YiMenu(MENU_TOOLS);
        var viewMenu = new YiMenu(MENU_VIEW);
        var windowMenu = new YiMenu(MENU_WINDOW);
        var helpMenu = new YiMenu(MENU_HELP);

        getMenus().addAll(fileMenu, editMenu, toolsMenu, viewMenu, windowMenu, helpMenu);

        if (Yi.isRunningFromSource()) {
            var debugMenu = new Menu("Debug");
            {
                for (Language l : Language.getSupportedLanguages()) {
                    var languageItem = new MenuItem(l.getName());
                    languageItem.setOnAction(e -> I18n.setCurrentLanguage(l));
                    debugMenu.getItems().add(languageItem);
                }
            }
            getMenus().add(debugMenu);
        }

        setUseSystemMenuBar(true);
    }

    private Menu createFileMenu(EditorFrame frame) {
        var fileMenu = new YiMenu(MENU_FILE);

        var newGame = new YiMenuItem(ITEM_NEW_GAME);
        newGame.setAccelerator(GuiUtilities.getKeyCombination(KeyCode.N, KeyModifier.SHORTCUT));
        newGame.setOnAction(event -> {
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
                var newModel = new GameModel(19, 19, GameRules.CHINESE);
                frame.setGameModel(newModel);
            }
        });

        var open = new YiMenuItem(ITEM_OPEN_GAME);
        open.setAccelerator(GuiUtilities.getKeyCombination(KeyCode.O, KeyModifier.SHORTCUT));
        open.setOnAction(event -> {
            var fileChooser = new FileChooser();
            fileChooser.setTitle(ITEM_OPEN_GAME.getLocalisedText());
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
        });

        var save = new YiMenuItem(ITEM_SAVE_GAME);
        save.setAccelerator(GuiUtilities.getKeyCombination(KeyCode.S, KeyModifier.SHORTCUT));
        save.setOnAction(event -> {
            var existingModel = frame.getGameModel();
            Path saveFilePath = existingModel.getLastSavePath();

            if (saveFilePath == null) {
                saveFilePath = promptAndStoreSaveFile(existingModel, frame);
            }

            if (saveFilePath != null && existingModel.getLastSaveFormat() != null) {
                GameModelExporter.INSTANCE.toFile(existingModel, saveFilePath, existingModel.getLastSaveFormat());
            }
        });

        var saveAs = new YiMenuItem(ITEM_SAVE_AS_GAME);
        saveAs.setAccelerator(GuiUtilities.getKeyCombination(KeyCode.S, KeyModifier.SHORTCUT, KeyModifier.SHIFT));
        saveAs.setOnAction(event -> {
            var existingModel = frame.getGameModel();
            Path saveFilePath = promptAndStoreSaveFile(existingModel, frame);
            FileFormat saveFileFormat = existingModel.getLastSaveFormat();

            if (saveFilePath != null) {
                GameModelExporter.INSTANCE.toFile(existingModel, saveFilePath, saveFileFormat);
            }
        });

        fileMenu.getItems().addAll(newGame, open, save, saveAs);

        return fileMenu;
    }

    /*
        TODO:
            1. Wrapper around FileFormat to provide an ExtensionFilter for FileChooser for
               save/load
            2. For now we're assuming the format being saved is SGF, but we need to determine
               this from the selected extension filter.
     */

    private Path promptAndStoreSaveFile(@NotNull GameModel gameModel, EditorFrame frame) {
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
