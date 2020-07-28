package yi.editor.components;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Primary toolbar for {@link yi.editor.EditorFrame} that displays a set of supported editing tools
 * and other options.
 */
public class EditorToolBar extends ToolBar {

    public EditorToolBar() {
        addButton("/icons/annoTriangle32.png");
        addButton("/icons/annoCircle32.png");
        addButton("/icons/annoSquare32.png");
        addButton("/icons/annoCross32.png");
    }

    private void addButton(String s) {
        var btn1 = new Button();
        var icon = new ImageView(new Image(EditorToolBar.class.getResourceAsStream(s)));
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        btn1.setGraphic(icon);
        getItems().add(btn1);
    }

}
