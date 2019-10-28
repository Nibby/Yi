package codes.nibby.yi.editor.component;

import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.utility.UiUtility;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

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
                editorWindow.createDocument();
            });

            Button btnOpen = new Button("", UiUtility.getFxIcon("/icons/open_invert.png", iconSize, iconSize));
            btnOpen.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                editorWindow.openDocument();
            });

            Button btnSave = new Button("", UiUtility.getFxIcon("/icons/save_invert.png", iconSize, iconSize));
            Button btnSaveAs = new Button("", UiUtility.getFxIcon("/icons/save_invert.png", iconSize, iconSize));

            tbLeft.getItems().addAll(btnNew, btnOpen, btnSave, btnSaveAs);
        }
        setCenter(tbLeft);

        tbRight = new ToolBar();
        {
            String[] perspectives = new String[]{
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
