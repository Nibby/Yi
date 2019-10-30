package codes.nibby.yi.editor.layout;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.editor.GameEditorWindow;
import javafx.scene.layout.BorderPane;

/**
 * A collection of components and parameters used to construct a perspective.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public abstract class AbstractLayout {

    private BorderPane contentPane;
    private GameEditorWindow editor;

    private boolean showTopSidebar = false;
    private boolean showLeftSidebar = false;
    private boolean showRightSidebar = false;
    private boolean showBottomSidebar = false;

    public AbstractLayout(GameEditorWindow editor) {
        this.editor = editor;

        contentPane = createLayout();
        if (contentPane == null)
            contentPane = new BorderPane();
        contentPane.setTop(getEditor().getToolBar());
    }

    /**
     * Returns a properly constructed perspective based on editor settings.
     *
     * @param editor Editor to be applied a layout.
     * @return The new layout.
     */
    public static AbstractLayout generate(GameEditorWindow editor) {
        LayoutType p = Config.getEditorLayout();
        switch (p) {
            case EDIT:
                return new ReviewLayout(editor);
            case ANALYSIS:
                return new AnalysisLayout(editor);
            case PRESENTER:
                return new PresenterLayout(editor);

            default:
                throw new IllegalArgumentException("Bad perspective: " + p.name());
        }
    }

    protected abstract BorderPane createLayout();

    public boolean isShowTopSidebar() {
        return showTopSidebar;
    }

    public void setShowTopSidebar(boolean showTopSidebar) {
        this.showTopSidebar = showTopSidebar;
    }

    public boolean isShowLeftSidebar() {
        return showLeftSidebar;
    }

    public void setShowLeftSidebar(boolean showLeftSidebar) {
        this.showLeftSidebar = showLeftSidebar;
    }

    public boolean isShowingRightSidebar() {
        return showRightSidebar;
    }

    public void setShowRightSidebar(boolean showRightSidebar) {
        this.showRightSidebar = showRightSidebar;
    }

    public boolean isShowBottomSidebar() {
        return showBottomSidebar;
    }

    public void setShowBottomSidebar(boolean showBottomSidebar) {
        this.showBottomSidebar = showBottomSidebar;
    }

    GameEditorWindow getEditor() {
        return editor;
    }

    public BorderPane getContentPane() {
        return contentPane;
    }
}
