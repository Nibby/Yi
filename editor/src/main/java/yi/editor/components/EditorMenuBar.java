package yi.editor.components;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import yi.core.go.GameModel;
import yi.core.go.GameModelImporter;
import yi.core.go.GameParseException;
import yi.core.go.GameRules;
import yi.editor.EditorFrame;

import java.io.File;
import java.io.IOException;

/**
 * Primary menu bar for {@link yi.editor.EditorFrame}.
 */
// TODO: Extract labels into resource keys for internationalization
public class EditorMenuBar extends MenuBar {

    public EditorMenuBar(EditorFrame frame) {
        var fileMenu = createFileMenu(frame);

        var editMenu = new Menu("Edit");
        var toolsMenu = new Menu("Tools");
        var viewMenu = new Menu("View");
        var windowMenu = new Menu("Window");
        var helpMenu = new Menu("Help");

        getMenus().addAll(fileMenu, editMenu, toolsMenu, viewMenu, windowMenu, helpMenu);
        setUseSystemMenuBar(true);
    }

    private Menu createFileMenu(EditorFrame frame) {
        var fileMenu = new Menu("File");

        var newGame = new MenuItem("New Game");
        newGame.setOnAction(event -> {
            // TODO: Show a new dialog prompting for new game document information.
            //       The values below are hard-coded, and are temporary.
            var newModel = new GameModel(19, 19, GameRules.CHINESE);
            frame.setGameModel(newModel);
        });

        var open = new MenuItem("Open Game");
        open.setOnAction(event -> {
            var fileChooser = new FileChooser();
            fileChooser.setTitle("Open Game");
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

        var save = new MenuItem("Save");
        save.setOnAction(event -> {
            // TODO: Implement me
        });

        var saveAs = new MenuItem("Save As");
        saveAs.setOnAction(event -> {
            // TODO: Implement me
        });

        fileMenu.getItems().addAll(newGame, open, save, saveAs);

        return fileMenu;
    }

}
