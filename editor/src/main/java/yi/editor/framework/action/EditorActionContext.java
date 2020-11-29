package yi.editor.framework.action;

import org.jetbrains.annotations.NotNull;
import yi.editor.EditorFrame;

/**
 * Helper class to aid in the execution of an {@link EditorAction}. This class
 * mainly provides the context in which the action is run, such as the current
 * editor window that triggered the action.
 */
public final class EditorActionContext {

    private final EditorFrame frame;
    private EditorAction invokedAction;

    public EditorActionContext(@NotNull EditorFrame frame) {
        this.frame = frame;
    }

    /**
     * @return The currently active editor window invoking the action.
     */
    public @NotNull EditorFrame getEditorFrame() {
        return frame;
    }

    public EditorAction getInvokedAction() {
        return invokedAction;
    }

    public void setInvokedAction(EditorAction invokedAction) {
        this.invokedAction = invokedAction;
    }
}
