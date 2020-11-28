package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.BooleanProperty;
import yi.common.i18n.TextResource;
import yi.component.YiRadioMenuItem;

import java.util.function.Consumer;

/**
 * A stateful action item that exports to either a {@link javafx.scene.control.RadioButton}
 * as component, or {@link YiRadioMenuItem} as menu item. This action can be assigned a
 * {@link ToggleGroup} to enforce selection constraints.
 *
 * @see EditorAtomicAction
 */
public class EditorRadioAction extends EditorAbstractAction {

    private final BooleanProperty selectedProperty = new BooleanProperty(false);

    public EditorRadioAction(TextResource name, @Nullable Consumer<EditorActionHelper> action) {
        this(null, name, action);
    }

    public EditorRadioAction(@Nullable EditorActionManager managerInstance, TextResource name, Consumer<EditorActionHelper> action) {
        super(managerInstance, name, action);

        selectedProperty.addListener(newValue -> {
            getCachedMenuItem().ifPresent(item -> {
                assert item instanceof YiRadioMenuItem;
                ((YiRadioMenuItem) item).setSelected(newValue);
            });
        });
    }

    @Override
    protected @NotNull MenuItem getAsMenuItemImpl() {
        return new YiRadioMenuItem(getName(), getIcon());
    }

    @Override
    protected Node getAsComponentImpl() {
        return null; // TODO: Create a radio button
    }

    @Override
    protected void onIconUpdate(ImageView newIcon) {

    }

    @Override
    protected void onNameUpdate(TextResource newName) {

    }

    @Override
    protected void onEnabledStateUpdate(boolean isEnabledNow) {

    }

    @Override
    protected void onVisibilityUpdate(boolean isVisibleNow) {

    }

    public void setMenuToggleGroup(ToggleGroup group) {
        MenuItem menuItem = getAsMenuItem();
        group.getToggles().add((YiRadioMenuItem) menuItem);
    }

    public void setSelected(boolean isSelected) {
        selectedProperty.set(isSelected);
    }
}
