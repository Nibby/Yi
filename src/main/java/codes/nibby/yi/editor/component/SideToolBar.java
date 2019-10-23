package codes.nibby.yi.editor.component;

import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.rules.GameRules;
import codes.nibby.yi.io.GameFileParser;
import codes.nibby.yi.io.GameParseException;
import codes.nibby.yi.io.UnsupportedFileTypeException;
import codes.nibby.yi.utility.UiUtility;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * The top level sidebar tool bar.
 *
 * @author Kevin Yang
 * Created on 1 October 2019
 */
public class SideToolBar extends BorderPane {

    private ToolBar tbLeft, tbRight;
    private GameEditorWindow editorWindow;

    public SideToolBar(GameEditorWindow editorWindow) {
        final int iconSize = 16;
        this.editorWindow = editorWindow;
        tbLeft = new ToolBar();
        {
            Button btnNew = new Button("", UiUtility.getFxIcon("/icons/new_invert.png", iconSize, iconSize));
            btnNew.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                // TODO check if document needs saving
//                ResourceBundle bundle = Config.getLanguage().getResourceBundle("GameEditorWindow");
//                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                alert.setTitle(bundle.getString(""));

                Game game = new Game(GameRules.CHINESE, 19, 19);
                editorWindow.setGame(game);
            });

            Button btnOpen = new Button("", UiUtility.getFxIcon("/icons/open_invert.png", iconSize, iconSize));
            btnOpen.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                // TODO temporary
                FileChooser fc = UiUtility.createGameRecordOpenFileChooser("Open file", Paths.get(System.getProperty("user.home")));
                File file = fc.showOpenDialog(editorWindow);
                try {
                    Game game = GameFileParser.parse(file);
                    if (game != null)
                        editorWindow.setGame(game);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (GameParseException ex) {
                    ex.printStackTrace();
                } catch (UnsupportedFileTypeException ex) {
                    ex.printStackTrace();
                }
            });

            Button btnSave = new Button("", UiUtility.getFxIcon("/icons/save_invert.png", iconSize, iconSize));
            Button btnSaveAs = new Button("", UiUtility.getFxIcon("/icons/save_invert.png", iconSize, iconSize));

            tbLeft.getItems().addAll(btnNew, btnOpen, btnSave, btnSaveAs);
        }
        setCenter(tbLeft);

        tbRight = new ToolBar();
        {
            String[] perspectives = new String[] {
                "Review", "AI Analysis", "Presentation"
            };
            ComboBox<String> comboPerspective = new ComboBox<>(FXCollections.observableArrayList(perspectives));
            comboPerspective.setEditable(false);
            tbRight.getItems().addAll(comboPerspective);
        }
        setRight(tbRight);

        getStyleClass().add("editor_side_toolbar");
    }

}
