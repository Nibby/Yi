package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.i18n.TextResource;
import yi.component.YiMenuItem;

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
}
