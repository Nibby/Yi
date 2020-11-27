package yi.editor.components;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import yi.component.YiCheckMenuItem;
import yi.component.YiMenu;
import yi.component.YiMenuItem;
import yi.component.YiRadioMenuItem;
import yi.component.i18n.I18n;
import yi.component.i18n.Language;
import yi.component.utilities.SystemUtilities;
import yi.core.go.*;
import yi.core.go.docformat.FileFormat;
import yi.editor.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static yi.editor.TextKeys.*;

/**
 * Primary menu bar for {@link yi.editor.EditorFrame}.
 */
// TODO: This class is one big work in progress
public class EditorMenuBar extends MenuBar {

    public EditorMenuBar(EditorFrame frame) {
        var fileMenu = createFileMenu(frame);
        var editMenu = createEditMenu(frame);
        var toolsMenu = new YiMenu(MENU_TOOLS);
        var viewMenu = createViewMenu(frame);
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

        if (SystemUtilities.isMac()) {
            setUseSystemMenuBar(true);
        }
    }

    private YiMenu createViewMenu(EditorFrame frame) {
        var viewMenu = new YiMenu(MENU_VIEW);
        {
            var currentLayout = frame.getContentLayout();
            var radioGroup = new ToggleGroup();
            var perspectiveMenu = new YiMenu(MENU_PERSPECTIVE);
            var layoutToItemMap = new HashMap<ContentLayout, YiRadioMenuItem>();

            for (ContentLayout layout : ContentLayout.values()) {
                var menuItem = new YiRadioMenuItem(layout.getFriendlyName());
                menuItem.setOnAction(e -> frame.setLayout(layout));
                menuItem.setSelected(layout == currentLayout);

                var layoutAcceleratorId = layout.getAcceleratorId();
                AcceleratorManager.getAccelerator(layoutAcceleratorId).install(menuItem);

                radioGroup.getToggles().add(menuItem);
                perspectiveMenu.getItems().add(menuItem);
                layoutToItemMap.put(layout, menuItem);
            }

            frame.addContentLayoutChangeListener(newLayout -> {
                var menuItemToSelect = layoutToItemMap.get(newLayout);
                if (menuItemToSelect == null) {
                    // Only reason I can think of for this to happen is when Java GC'd
                    // the HashMap storing this. I think the solution to that might be
                    // to elevate the map to a private field variable...
                    throw new IllegalStateException("Cannot find matching menu item to " +
                            "select for layout: " + newLayout.name());
                }
                menuItemToSelect.setSelected(true);
            });

            viewMenu.getItems().add(perspectiveMenu);
        }

        { // Toggle coordinates
            var toggleCoordinates = new YiCheckMenuItem(TextKeys.MENUITEM_TOGGLE_COORDINATES);
            toggleCoordinates.setOnAction(e -> {
                var showIt = toggleCoordinates.isSelected();
                frame.getBoardViewer().setShowCoordinates(showIt);
            });
            AcceleratorManager.getAccelerator(AcceleratorId.TOGGLE_BOARD_COORDINATES).install(toggleCoordinates);
            viewMenu.getItems().add(toggleCoordinates);
        }

        return viewMenu;
    }

    private YiMenu createEditMenu(EditorFrame frame) {
        return new YiMenu(MENU_EDIT);
    }

    private Menu createFileMenu(EditorFrame frame) {
        var fileMenu = new YiMenu(MENU_FILE);

        var newGame = new YiMenuItem(MENUITEM_NEW_GAME);
        AcceleratorManager.getAccelerator(AcceleratorId.NEW_GAME).install(newGame);
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

        var open = new YiMenuItem(MENUITEM_OPEN_GAME);
        AcceleratorManager.getAccelerator(AcceleratorId.OPEN_GAME).install(open);
        open.setOnAction(event -> {
            var fileChooser = new FileChooser();
            fileChooser.setTitle(MENUITEM_OPEN_GAME.getLocalisedText());
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

        var save = new YiMenuItem(MENUITEM_SAVE_GAME);
        AcceleratorManager.getAccelerator(AcceleratorId.SAVE_GAME).install(save);
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

        var saveAs = new YiMenuItem(MENUITEM_SAVE_AS_GAME);
        AcceleratorManager.getAccelerator(AcceleratorId.SAVE_AS_GAME).install(saveAs);
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
