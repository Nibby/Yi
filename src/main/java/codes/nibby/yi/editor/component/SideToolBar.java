package codes.nibby.yi.editor.component;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
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
        tbLeft = new ToolBar();
        tbLeft.getItems().add(new Button("Open"));
        tbLeft.getItems().add(new Button("Save"));
        setCenter(tbLeft);

        tbRight = new ToolBar();
        tbRight.getItems().add(new Button("Perspective"));
        setRight(tbRight);

        getStyleClass().add("editor_side_toolbar");
    }

}
