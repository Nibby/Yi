package yi.editor.framework.action;

import yi.editor.EditorWindow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores all actions created in the editor module.
 * <p/>
 * Each {@link EditorWindow} instantiates with an instance of this class, which will
 * manage all the actions that are specific to that frame.
 *
 * @see EditorAction Action system contract
 * @see EditorAbstractAction Base implementation
 */
public final class EditorActionManager {

    private final Set<EditorAction> allActions = new HashSet<>();

    private final EditorActionContext context;

    public EditorActionManager(EditorWindow frame) {
        this.context = new EditorActionContext(frame);
    }

    /**
     * @return All the actions currently registered to this manager.
     * The result set is immutable.
     */
    public Set<EditorAction> getAllActions() {
        synchronized (allActions) {
            return Collections.unmodifiableSet(allActions);
        }
    }

    /**
     * Registers an action to this manager, which works specifically with
     * the {@link EditorWindow} containing it.
     *
     * @param action Action to be registered.
     */
    public void addAction(EditorAction action) {
        synchronized (allActions) {
            allActions.add(action);
        }
    }

    /**
     * Register a series of actions to this manager, which works specifically
     * with the {@link EditorWindow} containing it.
     *
     * @param actions Array of actions to be registered.
     */
    public void addActions(EditorAction[] actions) {
        for (EditorAction action : actions) {
            addAction(action);
        }
    }

    /**
     * @return Helper class to aid the execution of actions for this editor instance.
     * @see EditorActionContext
     */
    public EditorActionContext getContext() {
        return context;
    }
}
