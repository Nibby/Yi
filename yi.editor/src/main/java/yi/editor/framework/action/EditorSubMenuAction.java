package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.component.YiMenu;
import yi.common.i18n.TextResource;
import yi.editor.EditorMainMenuType;
import yi.editor.framework.accelerator.EditorAcceleratorId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * A special type of action designed to enable a tree-like action hierarchy. As a menu item,
 * it will appear as a sub-menu. Other actions can be added to the sub-menu by calling 
 * {@link #addChildAction(EditorAction)}. This action by itself does not do any work when
 * selected.
 * <p/>
 * Currently exporting to component is not supported.
 */
public class EditorSubMenuAction extends EditorAbstractAction<Menu, Node> {

    private final Set<EditorAction> children = new HashSet<>();

    public EditorSubMenuAction(@NotNull TextResource name) {
        super(name, null);
    }

    /**
     * Adds an action to the children. This method can be used to create multiple
     * layers of sub-menus. Use this design with prudence as it can make actions
     * difficult to find.
     *
     * @param child Child action to be added under this sub-menu.
     */
    public void addChildAction(EditorAction child) {
        if (child instanceof EditorAbstractAction) {
            ((EditorAbstractAction<?, ?>) child).setParentAction(this);
        }
        children.add(child);
    }

    @Override
    protected @NotNull Menu getAsMenuItemImpl() {
        var menu = new YiMenu(getText(), getIcon());

        var sortedChildren = new ArrayList<>(children);
        sortedChildren.sort(Comparator.comparingDouble(EditorAction::getMenuPosition));

        for (EditorAction child : sortedChildren) {
            var childMenuItem = child.getAsMenuItem();
            menu.getItems().add(childMenuItem);
            child.markAsAddedToMenu();
        }

        return menu;
    }

    @Override
    protected Node getAsComponentImpl() {
        return null; // TODO: Work out what to do with sub-menu components, button with a context menu popup?
    }

    // Methods overridden to enable co-variant return types for smoother method chaining

    @Override
    public EditorAction setAccelerator(EditorAcceleratorId acceleratorId) {
        throw new UnsupportedOperationException("EditorSubMenuAction does not support accelerators");
    }

    @Override
    public EditorSubMenuAction setInMainMenu(@NotNull EditorMainMenuType menu, double position) {
        super.setInMainMenu(menu, position);
        return this;
    }

    @Override
    public EditorSubMenuAction setText(@NotNull TextResource i18nKey) {
        super.setText(i18nKey);
        return this;
    }

    @Override
    public EditorSubMenuAction setIcon(@Nullable ImageView icon) {
        super.setIcon(icon);
        return this;
    }

    @Override
    public EditorSubMenuAction setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    @Override
    public EditorSubMenuAction setVisible(boolean visible) {
        super.setVisible(visible);
        return this;
    }
}
