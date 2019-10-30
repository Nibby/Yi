package codes.nibby.yi.editor.layout;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.editor.component.GameTreePane;
import codes.nibby.yi.editor.component.GameTreeToolBar;
import codes.nibby.yi.editor.component.MoveCommentPane;
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
    private BorderPane rightSidebarPane;

    private BorderPane boardPane;
    private BorderPane content;

    private double lastDividerLocation;

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
        String treeTabText = lang.getString("sidebar.tabpane.gametree");
        Tab treeTab = new Tab(treeTabText, treeComponent);
        treeTabPane.getTabs().add(treeTab);

        MoveCommentPane commentViewer = getEditor().getMoveCommentPane();
        commentViewer.setMinHeight(120);
        commentViewer.setPrefHeight(120);
        splitSidebar = new SplitPane(treeTabPane, commentViewer);
        splitSidebar.setOrientation(Orientation.VERTICAL);
        splitSidebar.setDividerPositions(0.7d);

        rightSidebarPane = new BorderPane();
        rightSidebarPane.setCenter(splitSidebar);
        SideToolBar sideToolBar = new SideToolBar(getEditor());
        rightSidebarPane.setTop(sideToolBar);
        boolean visible = isShowingRightSidebar();
        rightSidebarPane.setVisible(visible);
        rightSidebarPane.setManaged(visible);
        rightSidebarPane.setMinWidth(265);

        // Components in the centre
        boardPane = new BorderPane();
        boardPane.setCenter(getEditor().getGameBoard());
        boardPane.setMinWidth(600);
        splitMain = new SplitPane(boardPane, rightSidebarPane);
        splitMain.setOrientation(Orientation.HORIZONTAL);
        lastDividerLocation = visible ? 0.685d : 1.0d;
        splitMain.setDividerPositions(lastDividerLocation);

        content = new BorderPane();
        content.setCenter(splitMain);
        content.widthProperty().addListener(evt -> {
            if (isShowingRightSidebar()) {
                splitMain.setDividerPosition(0, lastDividerLocation);
            } else {
                splitMain.setDividerPosition(0, 1.0d);
            }

        });
        return content;
    }

    @Override
    public void setShowRightSidebar(boolean showRightSidebar) {
        super.setShowRightSidebar(showRightSidebar);

        rightSidebarPane.setVisible(showRightSidebar);
        rightSidebarPane.setManaged(showRightSidebar);
        if (showRightSidebar) {
            splitMain.setDividerPosition(0, lastDividerLocation);
        } else {
            lastDividerLocation = splitMain.getDividers().get(0).getPosition();
            splitMain.setDividerPosition(0, 1.0d);
        }
    }
}
