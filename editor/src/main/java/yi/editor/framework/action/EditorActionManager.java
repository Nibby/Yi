package yi.editor.framework.action;

import yi.editor.EditorFrame;
import yi.editor.components.EditorPerspective;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages all shared and instance-specific actions created in the editor module.
 * Each {@link EditorFrame} instantiates with an instance of this class, which will
 * manage all the actions that are specific to that frame. On the other hand, this
 * class also maintains a static list of stateless, global actions that are universally
 * used across multiple instances of {@link EditorFrame}.
 *
 * @see EditorAction Action system contract
 * @see EditorAbstractAction Base implementation
 */
public final class EditorActionManager {

    private static final Set<EditorAction> SHARED_ACTIONS = new HashSet<>();
    private final Set<EditorAction> instanceActions = new HashSet<>();

    private final EditorActionContext context;

    public EditorActionManager(EditorFrame frame) {
        this.context = new EditorActionContext(frame);

        EditorPerspective.initializeActions(this);
    }

    /**
     * Registers an action so that it is universally available. This means the action
     * does not work with any {@link EditorFrame} instance-specific data, such as its
     * current view layout.
     *
     * @param action Shared action to be registered.
     */
    public static synchronized void registerSharedAction(EditorAction action) {
        SHARED_ACTIONS.add(action);
    }

    /**
     * @return All the actions, both shard and instance-specific, currently registered
     * to this manager. The returned result is immutable.
     */
    public synchronized Set<EditorAction> getAllActions() {
        var result = new HashSet<EditorAction>();
        result.addAll(getSharedActions());
        result.addAll(getInstanceActions());
        return result;
    }

    /**
     * @return All the shared actions registered via {@link #registerSharedAction(EditorAction)}
     * so far. The returned result is immutable.
     */
    public static synchronized Set<EditorAction> getSharedActions() {
        return Collections.unmodifiableSet(SHARED_ACTIONS);
    }

    /**
     * @return All the instance-specific actions registered via
     * {@link #registerInstanceSpecificAction(EditorAction)} so far. The returned result
     * is immutable.
     */
    public synchronized Set<EditorAction> getInstanceActions() {
        return Collections.unmodifiableSet(instanceActions);
    }

    /**
     * Registers an action to this instance action manager, which works specifically with
     * the {@link EditorFrame} containing it. Usually this means the action works with
     * frame-specific states, such as its current view layout.
     * <p/>
     * For actions that do not dependent on any state, consider using
     * {@link #registerSharedAction(EditorAction)} instead.
     *
     * @param action Instance-specific action to be registered.
     */
    public void registerInstanceSpecificAction(EditorAction action) {
        instanceActions.add(action);
    }

    /**
     * @return Helper class to aid the execution of actions for this editor instance.
     * @see EditorActionContext
     */
    public EditorActionContext getContext() {
        return context;
    }
}
