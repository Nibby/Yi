package codes.nibby.yi.app.framework.action;

import codes.nibby.yi.app.framework.AppWindow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores all actions created in the editor module.
 * <p/>
 * Each {@link AppWindow} instantiates with an instance of this class, which will
 * manage all the actions that are specific to that window.
 *
 * @see AppAction Action system contract
 * @see AppAbstractAction Base implementation
 */
public final class AppActionManager {

    private final Set<AppAction> allActions = new HashSet<>();

    private final AppActionContext context;

    public AppActionManager(AppWindow window) {
        this.context = new AppActionContext(window);
    }

    /**
     * @return All the actions currently registered to this manager.
     * The result set is immutable.
     */
    public Set<AppAction> getAllActions() {
        synchronized (allActions) {
            return Collections.unmodifiableSet(allActions);
        }
    }

    /**
     * Registers an action to this manager, which works specifically with
     * the {@link AppWindow} containing it.
     *
     * @param action Action to be registered.
     */
    public void addAction(AppAction action) {
        synchronized (allActions) {
            allActions.add(action);
        }
    }

    /**
     * Register a series of actions to this manager, which works specifically
     * with the {@link AppWindow} containing it.
     *
     * @param actions Array of actions to be registered.
     */
    public void addActions(AppAction[] actions) {
        for (AppAction action : actions) {
            addAction(action);
        }
    }

    /**
     * @return Helper class to aid the execution of actions for this editor instance.
     * @see AppActionContext
     */
    public AppActionContext getContext() {
        return context;
    }

    public void refreshActions() {
        getAllActions().forEach(action -> action.refreshState(context));
    }
}
