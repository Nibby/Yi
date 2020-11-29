package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.BooleanProperty;
import yi.common.i18n.TextResource;
import yi.component.YiCheckMenuItem;

import java.util.function.Consumer;

public class EditorToggleAction extends EditorAbstractAction {

    private final BooleanProperty selectedProperty = new BooleanProperty(false);

    public EditorToggleAction(TextResource name,
                              @Nullable Consumer<EditorActionContext> action) {
        this(null, name, action);
    }

    /**
     * Instantiates an action and registers it to the action manager. If an action manager
     * instance is provided, the action is considered instance-specific. Otherwise the
     * action is added to the global shared action list.
     *
     * @param manager Action manager instance, may be null to indicate a shared action.
     *                Or if this is non-null, indicates an instance-specific action.
     * @param name    Locale-agnostic name of this action.
     * @param action  Task to perform when this action is executed.
     */
    public EditorToggleAction(@Nullable EditorActionManager manager,
                              TextResource name,
                              @Nullable Consumer<EditorActionContext> action) {
        super(manager, name, action);

        selectedProperty.addListener(newValue -> {
            getCachedMenuItem().ifPresent(item -> {
                assert item instanceof YiCheckMenuItem;
                ((YiCheckMenuItem) item).setSelected(newValue);
            });
        });
    }

    @Override
    protected @NotNull MenuItem getAsMenuItemImpl() {
        return new YiCheckMenuItem(getName(), getIcon());
    }

    @Override
    protected @Nullable Node getAsComponentImpl() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setSelected(boolean isSelected) {
        selectedProperty.set(isSelected);
    }

    public boolean isSelected() {
        return selectedProperty.get();
    }
}
