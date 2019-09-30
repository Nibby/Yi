package codes.nibby.yi.editor.component;

import javafx.scene.control.ToolBar;

public class GameTreeToolBar extends ToolBar {

    private GameTreePane treePane;

    public GameTreeToolBar(GameTreePane treePane) {
        this.treePane = treePane;
        getStyleClass().add("game_tree_toolbar");
    }

}
