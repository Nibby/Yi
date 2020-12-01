package yi.editor.framework.action;

import org.jetbrains.annotations.NotNull;
import yi.editor.EditorWindow;

/**
 * Helper class to aid in the execution of an {@link EditorAction}. This class
 * mainly provides the context in which the action is run, such as the current
 * editor window that triggered the action.
 */
public final class EditorActionContext {

    private final EditorWindow window;

    public EditorActionContext(@NotNull EditorWindow window) {
        this.window = window;
    }

    /**
     * @return The currently active editor window invoking the action.
     */
    public @NotNull EditorWindow getEditorWindow() {
        return window;
    }
}
