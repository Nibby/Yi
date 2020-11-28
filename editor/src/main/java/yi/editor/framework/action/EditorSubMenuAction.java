package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.i18n.TextResource;
import yi.component.YiMenu;

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
public class EditorSubMenuAction extends EditorAbstractAction {

    private final Set<EditorAction> children = new HashSet<>();

    public EditorSubMenuAction(TextResource name) {
        this(null, name);
    }

    public EditorSubMenuAction(@Nullable EditorActionManager manager, TextResource name) {
        super(manager, name, null);
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
            ((EditorAbstractAction) child).setParentAction(this);
        }
        children.add(child);
    }

    @Override
    protected @NotNull MenuItem getAsMenuItemImpl() {
        var menu = new YiMenu(getName(), getIcon());

        var sortedChildren = new ArrayList<>(children);
        sortedChildren.sort(Comparator.comparingDouble(EditorAction::getMenuPosition));

        for (EditorAction child : children) {
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
