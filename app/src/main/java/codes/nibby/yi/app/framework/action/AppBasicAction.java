package codes.nibby.yi.app.framework.action;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import codes.nibby.yi.app.framework.YiButton;
import codes.nibby.yi.app.framework.YiMenuItem;
import codes.nibby.yi.app.i18n.TextResource;
import codes.nibby.yi.app.components.AppMainMenuType;
import codes.nibby.yi.app.framework.AppAccelerator;

import java.util.function.Consumer;

/**
 * Standard concrete implementation of {@link AppAction} that represents one
 * repeatable, self-contained piece of functionality. This action is stateless.
 * <p/>
 * For stateful actions, see {@link AppRadioAction} and
 * {@link AppToggleAction}.
 */
public class AppBasicAction extends AppAbstractAction<YiMenuItem, Node> {

    public AppBasicAction(@NotNull TextResource name) {
        this(name, null);
    }

    public AppBasicAction(@NotNull TextResource name,
                          @Nullable Consumer<AppActionContext> action) {
        super(name, action);
    }

    @Override
    protected @NotNull YiMenuItem getAsMenuItemImpl() {
        return new YiMenuItem(getText(), getIcon());
    }

    @Override
    protected Node getAsComponentImpl() {
        return new YiButton(getText(), getIcon());
    }

    // Methods overridden to enable co-variant return types for smoother method chaining

    @Override
    public AppBasicAction setAccelerator(AppAccelerator acceleratorId) {
        super.setAccelerator(acceleratorId);
        return this;
    }

    @Override
    public AppBasicAction setInMenuBar(@NotNull AppMainMenuType menu, double position) {
        super.setInMenuBar(menu, position);
        return this;
    }

    @Override
    public AppBasicAction setText(@NotNull TextResource i18nKey) {
        super.setText(i18nKey);
        return this;
    }

    @Override
    public AppBasicAction setIcon(@Nullable ImageView icon) {
        super.setIcon(icon);
        return this;
    }

    @Override
    public AppBasicAction setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    @Override
    public AppBasicAction setVisible(boolean visible) {
        super.setVisible(visible);
        return this;
    }
}
