package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.i18n.TextResource;
import yi.component.YiMenuItem;

import java.util.function.Consumer;

/**
 * Standard concrete implementation of {@link EditorAction} that represents one repeatable,
 * self-contained piece of functionality. This action is stateless, so will always perform
 * the same task when selected.
 *
 * @see EditorRadioAction Radio-option actions
 */
public class EditorBasicAction extends EditorAbstractAction {

    public EditorBasicAction(TextResource name, @Nullable Consumer<EditorActionContext> action) {
        this(null, name, action);
    }

    public EditorBasicAction(@Nullable EditorActionManager manager, TextResource name,
                             @Nullable Consumer<EditorActionContext> action) {
        super(manager, name, action);
    }

    @Override
    protected @NotNull MenuItem getAsMenuItemImpl() {
        return new YiMenuItem(getName(), getIcon());
    }

    @Override
    protected Node getAsComponentImpl() {
        throw new UnsupportedOperationException("Not implemented");
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
}
