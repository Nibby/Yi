package codes.nibby.yi.app.framework.action;

import codes.nibby.yi.app.framework.AppWindow;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class to aid in the execution of an {@link AppAction}. This class
 * mainly provides the context in which the action is run, such as the current
 * editor window that triggered the action.
 */
public final class AppActionContext {

    private final AppWindow window;

    public AppActionContext(@NotNull AppWindow window) {
        this.window = window;
    }

    /**
     * @return The currently active editor window invoking the action.
     */
    public @NotNull AppWindow getInvokerWindow() {
        return window;
    }
}
