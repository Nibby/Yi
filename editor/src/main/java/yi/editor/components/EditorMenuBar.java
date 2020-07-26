package yi.editor.components;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

/**
 * Primary menu bar for {@link yi.editor.EditorFrame}
 */
public class EditorMenuBar extends MenuBar {

    public EditorMenuBar() {
        var fileMenu = new Menu("File");
        var helpMenu = new Menu("Help");

        getMenus().addAll(fileMenu, helpMenu);
    }

}
