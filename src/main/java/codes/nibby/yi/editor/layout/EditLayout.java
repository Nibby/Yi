package codes.nibby.yi.editor.layout;

import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.editor.component.MoveCommentPane;
import codes.nibby.yi.editor.component.GameTreePane;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class EditLayout extends AbstractLayout {

    private SplitPane splitMain;
    private SplitPane splitSidebar;

    private BorderPane boardPane;
    private BorderPane content;

    public EditLayout(GameEditorWindow editor) {
        super(editor);
    }

    @Override
    protected BorderPane createLayout() {
        GameTreePane treeViewer = getEditor().getGameTreePane();
        MoveCommentPane commentViewer = getEditor().getMoveCommentPane();
        splitSidebar = new SplitPane(treeViewer, commentViewer);
        splitSidebar.setOrientation(Orientation.VERTICAL);
        splitSidebar.setDividerPositions(0.7d);

        boardPane = new BorderPane();
        boardPane.setCenter(getEditor().getGameBoard());
        splitMain = new SplitPane(boardPane, splitSidebar);
        splitMain.setOrientation(Orientation.HORIZONTAL);
        splitMain.setDividerPositions(0.7d);

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
