package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.BooleanProperty;
import yi.common.i18n.TextResource;
import yi.component.YiCheckMenuItem;
import yi.editor.EditorMainMenuType;
import yi.editor.framework.accelerator.EditorAcceleratorId;

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

    /**
     * Sets the selected state of this toggle action. This will update
     * the status of exported components as well.
     *
     * @param isSelected true if this action is selected.
     */
    public void setSelected(boolean isSelected) {
        selectedProperty.set(isSelected);
    }

    /**
     * @return true if the action is currently selected.
     */
    public boolean isSelected() {
        return selectedProperty.get();
    }

    // Methods overridden to enable co-variant return types for smoother method chaining

    @Override
    public EditorToggleAction setAccelerator(EditorAcceleratorId acceleratorId) {
        super.setAccelerator(acceleratorId);
        return this;
    }

    @Override
    public EditorToggleAction setInMainMenu(@NotNull EditorMainMenuType menu, double position) {
        super.setInMainMenu(menu, position);
        return this;
    }

    @Override
    public EditorToggleAction setName(@NotNull TextResource name) {
        super.setName(name);
        return this;
    }

    @Override
    public EditorToggleAction setIcon(@Nullable ImageView icon) {
        super.setIcon(icon);
        return this;
    }

    @Override
    public EditorToggleAction setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    @Override
    public EditorToggleAction setVisible(boolean visible) {
        super.setVisible(visible);
        return this;
    }
}
