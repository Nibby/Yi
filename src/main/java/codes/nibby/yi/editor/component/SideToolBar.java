package codes.nibby.yi.editor.component;

import codes.nibby.yi.utility.IconUtility;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

/**
 * The top level sidebar tool bar.
 *
 * @author Kevin Yang
 * Created on 1 October 2019
 */
public class SideToolBar extends BorderPane {

    private ToolBar tbLeft, tbRight;

    public SideToolBar() {
        final int iconSize = 16;
        tbLeft = new ToolBar();
        {
            Button btnNew = new Button("", IconUtility.getFxIcon("/icons/new_invert.png", iconSize, iconSize));
            Button btnOpen = new Button("", IconUtility.getFxIcon("/icons/open_invert.png", iconSize, iconSize));
            Button btnSave = new Button("", IconUtility.getFxIcon("/icons/save_invert.png", iconSize, iconSize));
            Button btnSaveAs = new Button("", IconUtility.getFxIcon("/icons/save_invert.png", iconSize, iconSize));

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
