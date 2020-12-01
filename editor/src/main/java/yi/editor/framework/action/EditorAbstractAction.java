package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.BooleanProperty;
import yi.common.NullableProperty;
import yi.common.Property;
import yi.common.i18n.TextResource;
import yi.editor.EditorHelper;
import yi.editor.EditorMainMenuType;
import yi.editor.EditorTextResources;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.accelerator.EditorAcceleratorManager;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class is the base implementation for all actions in the editor module.
 */
public abstract class EditorAbstractAction implements EditorAction {

    private final Property<TextResource> nameProperty = new Property<>(EditorTextResources.EMPTY);
    private final NullableProperty<ImageView> iconProperty = new NullableProperty<>(null);
    private final BooleanProperty enabledProperty = new BooleanProperty(true);
    private final BooleanProperty visibleProperty = new BooleanProperty(true);
    private EditorAction parentAction = null;

    private final Consumer<EditorActionContext> action;

    private boolean addedToMenu = false;
    private EditorMainMenuType mainMenuType = null;
    private double menuPosition = 0d;

    private final NullableProperty<MenuItem> createdMenuItem = new NullableProperty<>(null);
    private final NullableProperty<Node> createdComponent = new NullableProperty<>(null);
    private final NullableProperty<EditorAcceleratorId> acceleratorId = new NullableProperty<>(null);

    /**
     * Instantiates an action.
     *
     * @param name Locale-agnostic name of this action.
     * @param action Task to perform when this action is executed. May be null, which
     *               does nothing on interaction.
     */
    public EditorAbstractAction(@NotNull TextResource name,
                                @Nullable Consumer<EditorActionContext> action) {

        this.nameProperty.set(name);
        this.action = action;

        addAcceleratorUpdateListener();
        addNameUpdateListener();
        addIconUpdateListener();
        addVisibilityListener();
        addEnabledStateListener();
    }

    private void addEnabledStateListener() {
        enabledProperty.addListener(isEnabledNow -> {
            getCachedMenuItem().ifPresent(item -> item.setDisable(!isEnabledNow));
            getCachedComponent().ifPresent(node -> node.setDisable(!isEnabledNow));
            onEnabledStateUpdate(isEnabledNow);
        });
    }

    private void addVisibilityListener() {
        visibleProperty.addListener(isVisibleNow -> {
            getCachedMenuItem().ifPresent(item -> item.setVisible(isVisibleNow));
            getCachedComponent().ifPresent(node -> {
                node.setVisible(isVisibleNow);
                node.setManaged(isVisibleNow);
            });
            onVisibilityUpdate(isVisibleNow);
        });
    }

    private void addIconUpdateListener() {
        iconProperty.addListener(newIcon -> {
            createdMenuItem.get().ifPresent(item -> item.setGraphic(newIcon));
            createdComponent.get().ifPresent(node -> {
                if (node instanceof Labeled) {
                    ((Labeled) node).setGraphic(newIcon);
                }
            });
            onIconUpdate(newIcon);
        });
    }

    private void addNameUpdateListener() {
        nameProperty.addListener(newName -> {
            createdMenuItem.get().ifPresent(item -> item.setText(newName.getLocalisedText()));
            createdComponent.get().ifPresent(node -> {
                if (node instanceof Labeled) {
                    ((Labeled) node).setText(newName.getLocalisedText());
                }
            });
            onNameUpdate(newName);
        });
    }

    private void addAcceleratorUpdateListener() {
        acceleratorId.addListener(newAcceleratorId -> createdMenuItem.get().ifPresent(menuItem -> installMenuItemAccelerator(newAcceleratorId, menuItem)));
        createdMenuItem.addListener(newMenuItem -> acceleratorId.get().ifPresent(id -> installMenuItemAccelerator(id, newMenuItem)));
    }

    private void installMenuItemAccelerator(EditorAcceleratorId acceleratorId, MenuItem menuItem) {
        if (EditorHelper.isRunningAsTest() && (acceleratorId == null || menuItem == null)) {
            // TODO: Supposedly UI tests aren't loading all the accelerators but
            //       I think it should. Work out what's causing this and try remove
            //       this exclusion if possible.
            return;
        }
        EditorAcceleratorManager.install(acceleratorId, menuItem);
    }

    @Override
    public @NotNull String getLocalisedName() {
        return nameProperty.get().getLocalisedText();
    }

    @Override
    public EditorAction setAccelerator(EditorAcceleratorId acceleratorId) {
        this.acceleratorId.set(acceleratorId);
        return this;
    }

    @Override
    public EditorAction setInMainMenu(@NotNull EditorMainMenuType menu, double position) {
        this.mainMenuType = menu;
        this.menuPosition = position;
        return this;
    }

    @Override
    public boolean isInMainMenu() {
        return mainMenuType != null;
    }

    @Override
    public double getMenuPosition() {
        return menuPosition;
    }

    @Override
    public @NotNull EditorMainMenuType getMainMenuType() {
        return Objects.requireNonNull(mainMenuType,
                "Action is not set to show in the main menu");
    }

    @Override
    public @NotNull MenuItem getAsMenuItem() {
        if (!isInMainMenu()) {
            throw new IllegalStateException("Cannot create menu item for action that is " +
                    "not set to appear in the main menu. Action: " + getLocalisedName());
        }
        Optional<MenuItem> menuItem = createdMenuItem.get();

        if (menuItem.isPresent()) {
            return menuItem.get();
        }

        MenuItem newMenuItem = getAsMenuItemImpl();
        newMenuItem.setUserData(this);
        newMenuItem.setVisible(isVisible());
        newMenuItem.setDisable(!isEnabled());
        createdMenuItem.set(newMenuItem);
        return newMenuItem;
    }

    protected abstract @NotNull MenuItem getAsMenuItemImpl();

    @Override
    public Node getAsComponent() {
        Optional<Node> component = createdComponent.get();

        if (component.isPresent()) {
            return component.get();
        }

        Node newComponent = getAsComponentImpl();
        if (newComponent != null) {
            newComponent.setUserData(this);
            newComponent.setVisible(isVisible());
            newComponent.setDisable(!isEnabled());
        }
        createdComponent.set(newComponent);
        return newComponent;
    }

    protected abstract @Nullable Node getAsComponentImpl();

    /**
     * Notifies that the icon for this action has changed.
     *
     * @param newIcon New icon for this action.
     *
     * @implNote The default implementation automatically updates
     * the icon of components that are subclasses of {@link Labeled}.
     */
    protected void onIconUpdate(ImageView newIcon) {

    }

    /**
     * Notifies that the name for this action has changed.
     *
     * @param newName New name for this action.
     *
     * @implNote The default implementation automatically updates
     * the text of components that are subclasses of {@link Labeled}.
     */
    protected void onNameUpdate(TextResource newName) {

    }

    /**
     * Notifies that the enabled state for this action has changed.
     *
     * @param isEnabledNow Current enabled status for this action.
     *
     * @implNote The default implementation automatically calls
     * {@link Node#setDisable(boolean)} on the exported menu item
     * and component.
     */
    protected void onEnabledStateUpdate(boolean isEnabledNow) {

    }

    /**
     * Notifies that the visibility of components for this action has
     * changed.
     *
     * @param isVisibleNow Current visibility status for this action.
     *
     * @implNote The default implementation automatically calls
     * {@link Node#setVisible(boolean)} and {@link Node#setManaged(boolean)}
     * for exported components, and {@link MenuItem#setVisible(boolean)}
     * for menu items.
     */
    protected void onVisibilityUpdate(boolean isVisibleNow) {

    }

    @Override
    public void performAction(EditorActionContext helper) {
        if (action != null) {
            action.accept(helper);
        }
    }

    @Override
    public @NotNull TextResource getName() {
        return nameProperty.get();
    }

    @Override
    public EditorAction setName(@NotNull TextResource name) {
        this.nameProperty.set(name);
        return this;
    }

    @Override
    public EditorAction setIcon(@Nullable ImageView icon) {
        this.iconProperty.set(icon);
        return this;
    }

    @Override
    public @Nullable ImageView getIcon() {
        return iconProperty.get().orElse(null);
    }

    @Override
    public EditorAction setEnabled(boolean enabled) {
        this.enabledProperty.set(enabled);
        return this;
    }

    @Override
    public boolean isEnabled() {
        return enabledProperty.get();
    }

    @Override
    public EditorAction setVisible(boolean visible) {
        this.visibleProperty.set(visible);
        return this;
    }

    @Override
    public boolean isVisible() {
        return visibleProperty.get();
    }

    @Override
    public void markAsAddedToMenu() {
        addedToMenu = true;
    }

    @Override
    public boolean isAddedToMenu() {
        return addedToMenu;
    }

    protected final void setParentAction(EditorSubMenuAction action) {
        this.parentAction = action;
    }

    @Override
    public boolean isTopLevelMenuItem() {
        return parentAction == null;
    }

    /**
     * This method only returns the cached result, and will not create a menu item.
     *
     * @return The currently cached menu item as a result for {@link #getAsMenuItem()}.
     * The returned result may be empty, which means either the action was not set to
     * appear in the main menu, or {@link #getAsMenuItem()} has not been called yet.
     */
    protected Optional<MenuItem> getCachedMenuItem() {
        return createdMenuItem.get();
    }

    /**
     * @return The currently cached component as a result of calling {@link #getAsComponent()}.
     * The returned result may be null, which means either the action does not support
     * exporting to component, or {@link #getAsComponent()} has not been called yet.
     */
    protected Optional<Node> getCachedComponent() {
        return createdComponent.get();
    }
}
