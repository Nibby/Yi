package codes.nibby.yi.editor.layout;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.editor.component.GameTreeToolBar;
import codes.nibby.yi.editor.component.MoveCommentPane;
import codes.nibby.yi.editor.component.GameTreePane;
import codes.nibby.yi.editor.component.SideToolBar;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.util.ResourceBundle;

public class ReviewLayout extends AbstractLayout {

    private SplitPane splitMain;
    private SplitPane splitSidebar;

    private BorderPane boardPane;
    private BorderPane content;

    public ReviewLayout(GameEditorWindow editor) {
        super(editor);
    }

    @Override
    protected BorderPane createLayout() {
        ResourceBundle lang = Config.getLanguage().getResourceBundle("GameEditorWindow");
        // Components on the sidebar

        GameTreePane treePane = getEditor().getGameTreePane();
        ScrollPane treeScrollPane = new ScrollPane(treePane);
        treeScrollPane.getStyleClass().add("game_tree_scroll_pane");
        {
            treeScrollPane.setPannable(true);
            treeScrollPane.setCursor(Cursor.OPEN_HAND);
            treeScrollPane.onMouseDraggedProperty().addListener(l -> {
                treeScrollPane.setCursor(Cursor.CLOSED_HAND);
            });
            treeScrollPane.onMouseDragReleasedProperty().addListener(l -> {
                treeScrollPane.setCursor(Cursor.OPEN_HAND);
            });
            treeScrollPane.setPadding(new Insets(2, 5, 2, 5));
            treePane.setScrollPane(treeScrollPane);
            treeScrollPane.getStyleClass().add("game_tree_scroll_pane");
        }

        BorderPane treeComponent = new BorderPane(treeScrollPane);
        treeComponent.setTop(new GameTreeToolBar(treePane));

        TabPane treeTabPane = new TabPane();
        treeTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        treeTabPane.getStyleClass().add("editor_sidebar_tabpane");
        String treeTabText = lang.getString("editor.sidebar.tabpane.gametree");
        Tab treeTab = new Tab(treeTabText, treeComponent);
        treeTabPane.getTabs().add(treeTab);

        MoveCommentPane commentViewer = getEditor().getMoveCommentPane();
        splitSidebar = new SplitPane(treeTabPane, commentViewer);
        splitSidebar.setOrientation(Orientation.VERTICAL);
        splitSidebar.setDividerPositions(0.7d);

        BorderPane sidebar = new BorderPane();
        sidebar.setCenter(splitSidebar);
        SideToolBar sideToolBar = new SideToolBar(getEditor());
        sidebar.setTop(sideToolBar);

        // Components in the centre
        boardPane = new BorderPane();
        boardPane.setCenter(getEditor().getGameBoard());
        splitMain = new SplitPane(boardPane, sidebar);
        splitMain.setOrientation(Orientation.HORIZONTAL);
        splitMain.setDividerPositions(0.685d);

        content = new BorderPane();
        content.setCenter(splitMain);
        return content;
    }

    @Override
    public void setGameTreePaneVisible(boolean flag) {
        super.setGameTreePaneVisible(flag);
        GameTreePane treePane = getEditor().getGameTreePane();
        treePane.setVisible(flag);
        treePane.setManaged(flag);

        updateSidebarState();
    }

    @Override
    public void setGameCommentPaneVisible(boolean flag) {
        super.setGameCommentPaneVisible(flag);
        MoveCommentPane commentViewer = getEditor().getMoveCommentPane();
        commentViewer.setVisible(flag);
        commentViewer.setManaged(flag);

        updateSidebarState();
    }

    /**
     * Check if the sidebar needs to be displayed.
     * It will be removed from the view if no components are visible inside.
     */
    private void updateSidebarState() {
        boolean treeVisible = getEditor().getGameTreePane().isVisible();
        boolean commentsVisible = getEditor().getMoveCommentPane().isVisible();
        boolean shouldShow = treeVisible || commentsVisible;

        splitSidebar.setVisible(shouldShow);
    }
}
