package codes.nibby.yi.editor.component;

import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.utility.UiUtility;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.util.ResourceBundle;

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
        ResourceBundle locale = editorWindow.getLocaleResourceBundle();
        {
            Button buttonNew = new Button("", UiUtility.getFxIcon("/icons/new_invert.png", iconSize, iconSize));
            buttonNew.setTooltip(new Tooltip(locale.getString("sidebar.button.new")));
            buttonNew.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                editorWindow.createDocument();
            });

            Button buttonOpen = new Button("", UiUtility.getFxIcon("/icons/open_invert.png", iconSize, iconSize));
            buttonOpen.setTooltip(new Tooltip(locale.getString("sidebar.button.open")));
            buttonOpen.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                editorWindow.openDocument();
            });

            Button buttonSave = new Button("", UiUtility.getFxIcon("/icons/save_invert.png", iconSize, iconSize));
            buttonSave.setTooltip(new Tooltip(locale.getString("sidebar.button.save")));
            buttonSave.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                editorWindow.saveDocument(false);
            });

            Button buttonSaveAs = new Button("", UiUtility.getFxIcon("/icons/save_invert.png", iconSize, iconSize));
            buttonSaveAs.setTooltip(new Tooltip(locale.getString("sidebar.button.save_as")));
            buttonSaveAs.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                editorWindow.saveDocument(true);
            });

            tbLeft.getItems().addAll(buttonNew, buttonOpen, buttonSave, buttonSaveAs);
        }
        setCenter(tbLeft);

        tbRight = new ToolBar();
        {
            String[] perspectives = new String[]{
                locale.getString("perspectives.edit"),
                locale.getString("perspectives.ai"),
                locale.getString("perspectives.presentation"),
            };
            ComboBox<String> comboPerspective = new ComboBox<>(FXCollections.observableArrayList(perspectives));
            comboPerspective.setEditable(false);
            tbRight.getItems().addAll(comboPerspective);
        }
        setRight(tbRight);

        getStyleClass().add("editor_side_toolbar");
    }

}
