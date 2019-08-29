package codes.nibby.yi.editor.perspective;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.editor.EditorBoardController;
import codes.nibby.yi.editor.GameEditorWindow;
import javafx.scene.layout.Pane;

/**
 * A collection of components and parameters used to construct a perspective.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public abstract class PerspectiveLayout {

    private Pane contentPane;
    private GameEditorWindow editor;

    public PerspectiveLayout(GameEditorWindow editor) {
        this.editor = editor;

        contentPane = createLayout();
    }

    protected abstract Pane createLayout();

    GameEditorWindow getEditor() {
        return editor;
    }

    public Pane getContentPane() {
        return contentPane;
    }

    /**
     * Returns a properly constructed perspective based on editor settings.
     *
     * @param editor Editor to be applied a layout.
     * @return The new layout.
     */
    public static PerspectiveLayout generate(GameEditorWindow editor) {
        Perspective p = Config.getEditorPerspective();
        switch (p) {
            case SIMPLE:
                return new SimplePerspectiveLayout(editor);
            case EDITOR:
                return new EditorPerspectiveLayout(editor);
            default:
                throw new IllegalArgumentException("Bad perspective: " + p.name());
        }
    }
}
