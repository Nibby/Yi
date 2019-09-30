package codes.nibby.yi.editor.component;

import javafx.scene.control.ToolBar;

/**
 * A set of display and functional tools for working with the game tree.
 *
 * @author Kevin Yang
 * Created on 1 October 2019
 */
public class GameTreeToolBar extends ToolBar {

    private GameTreePane treePane;

    public GameTreeToolBar(GameTreePane treePane) {
        this.treePane = treePane;
        getStyleClass().add("game_tree_toolbar");
    }

}
