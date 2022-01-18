package codes.nibby.yi.app.components;

import codes.nibby.yi.app.framework.action.AppAction;
import codes.nibby.yi.app.framework.action.AppActionManager;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents one aspect of the functionality in the {@link yi.editor.EditorWindow}. The
 * component may or may not supply a user interface item.
 *
 * @param <ComponentType> Type of user interface item represented by this component,
 *                       can set this to {@link Object} if it does not supply any.
 */
public interface AppComponent<ComponentType> {

    /**
     * Returns all the actions supplied by this component. This method may be called
     * multiple times so it is recommended to not return new action instances on each
     * call.
     *
     * @param actionManager Action manager for this editor window.
     * @return Actions supplied by this component. Return an empty array if this component
     * does not export any actions.
     */
    @NotNull AppAction[] getActions(AppActionManager actionManager);

    /**
     * Returns the user interface item supplied by this component. If this feature
     * is not applicable, return {@link Optional#empty()}.
     *
     * @return An optional user interface item representing this component.
     */
    Optional<ComponentType> getComponent();

}
