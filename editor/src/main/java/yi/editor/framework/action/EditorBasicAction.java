package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.i18n.TextResource;
import yi.component.YiMenuItem;
import yi.editor.EditorMainMenuType;
import yi.editor.framework.accelerator.EditorAcceleratorId;

import java.util.function.Consumer;

/**
 * Standard concrete implementation of {@link EditorAction} that represents one
 * repeatable, self-contained piece of functionality. This action is stateless.
 * <p/>
 * For stateful actions, see {@link EditorRadioAction} and
 * {@link EditorToggleAction}.
 */
public class EditorBasicAction extends EditorAbstractAction {

    public EditorBasicAction(@NotNull TextResource name,
                             @Nullable Consumer<EditorActionContext> action) {
        super(name, action);
    }

    @Override
    protected @NotNull MenuItem getAsMenuItemImpl() {
        return new YiMenuItem(getName(), getIcon());
    }

    @Override
    protected Node getAsComponentImpl() {
        throw new UnsupportedOperationException("Not implemented");
    }

    // Methods overridden to enable co-variant return types for smoother method chaining

    @Override
    public EditorBasicAction setAccelerator(EditorAcceleratorId acceleratorId) {
        super.setAccelerator(acceleratorId);
        return this;
    }

    @Override
    public EditorBasicAction setInMainMenu(@NotNull EditorMainMenuType menu, double position) {
        super.setInMainMenu(menu, position);
        return this;
    }

    @Override
    public EditorBasicAction setName(@NotNull TextResource name) {
        super.setName(name);
        return this;
    }

    @Override
    public EditorBasicAction setIcon(@Nullable ImageView icon) {
        super.setIcon(icon);
        return this;
    }

    @Override
    public EditorBasicAction setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    @Override
    public EditorBasicAction setVisible(boolean visible) {
        super.setVisible(visible);
        return this;
    }
}
