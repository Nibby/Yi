package codes.nibby.yi.app.framework.action;

import codes.nibby.yi.app.components.Accelerator;
import codes.nibby.yi.app.components.AppMainMenuType;
import codes.nibby.yi.app.components.AppMenuBar;
import codes.nibby.yi.app.framework.AppAccelerator;
import codes.nibby.yi.app.framework.AppText;
import codes.nibby.yi.app.framework.AppWindow;
import codes.nibby.yi.app.i18n.TextResource;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Defines one actionable task in the game editor. Each action must be atomic and
 * self-contained, in that it does not depend on another action to run.
 * <p/>
 * Actions can be set to show in the menu bar through {@link #setInMenuBar(AppMainMenuType, double)}.
 * Optionally, an action can be exported to a JavaFx {@link Node} via {@link #getAsComponent()}.
 *
 * @see AppActionManager
 */
public interface AppAction {

    /**
     * Sorts an action list in increasing order of {@link #getMenuPosition() menu position}.
     * @param allActions
     * @return
     */
    static List<? extends AppAction> sorted(List<? extends AppAction> allActions) {
        allActions.sort(Comparator.comparing(AppAction::getMenuPosition));
        return allActions;
    }

    /**
     * Sets the {@link AppActionContext} for this action. This is the main mechanism
     * for an action to distinguish which {@link AppWindow} it is working with.
     *
     * @apiNote There should be no reason to invoke this method outside of
     * {@link AppMenuBar} unless it is in a test case.
     *
     * @param context Current action context.
     */
    void setContext(AppActionContext context);

    /**
     * Sets the task to be performed when this action component (menu item or node) is
     * selected.
     *
     * @param action Tasks to perform when this action is selected.
     */
    void setAction(Consumer<AppActionContext> action);

    /**
     * @return Locale-specific text for the component(s) representing this action.
     */
    @NotNull String getLocalisedText();

    /**
     * Set the text to be displayed on the components of this action.
     * All text content are stored using i18n keys.
     *
     * @return {@link TextResource} used for localisation.
     * @see AppText
     */
    @NotNull TextResource getText();

    /**
     * Sets the {@link TextResource} used for localisation. All text resources in
     * the editor module are found in {@link AppText}.
     *
     * @param i18nKey Text resource for localisation.
     * @return this instance for method chaining.
     */
    AppAction setText(@NotNull TextResource i18nKey);

    /**
     * Sets whether or not to show the {@link #getIcon() action icon} on the exported
     * {@link #getAsMenuItem() menu item}.
     *
     * @param showIcon {@code true} if icon should be shown on the menu item.
     */
    void setShowIconOnMenuItem(boolean showIcon);

    /**
     * @return {@code true} if icon should be shown on the {@link #getAsMenuItem() menu item}.
     */
    boolean isShowingIconOnMenuItem();

    /**
     * Sets whether or not to show the {@link #getIcon() action icon} on the exported
     * {@link #getAsComponent() component}.
     *
     * @param showIcon {@code true} if icon should be shown on the component.
     */
    void setShowIconOnComponent(boolean showIcon);

    /**
     * @return {@code true} if icon should be shown on the {@link #getAsComponent()} component}.
     */
    boolean isShowingIconOnComponent();

    /**
     * Sets the graphic used on the exported action component. The menu component
     * should have the icon set always, but depending on implementation,
     * {@link #getAsComponent()} might not always return a component with the icon.
     *
     * @param icon Desired icon to show on the exported component.
     * @return this instance for method chaining.
     */
    AppAction setIcon(@Nullable ImageView icon);

    /**
     * @return Desired graphic to show on the exported component.
     */
    @Nullable ImageView getIcon();

    /**
     * Sets the shortcut key combination for the {@link #getAsMenuItem() menu component}.
     * <p/>
     * A list of all mapped accelerators can be found in {@link AppAccelerator}.
     *
     * @param acceleratorId Accelerator ID to be applied to the menu item component.
     * @return this instance for method chaining.
     */
    AppAction setAccelerator(AppAccelerator acceleratorId);

    /**
     * Sets whether this action should be added to the main application menu. If
     * this method is not called, then the action will not be added by default.
     *
     * @param menu Main menu category to appear under. It is possible to add this
     *             action to a sub-menu within the main category. In that case the
     *             menu type should still be the same top-level menu the sub-menu
     *             belongs to.
     * @param position Position of this menu item, a double value usually between
     *                 0 to 1.0, where 0 indicates the absolute top of the menu.
     *                 See {@link #getMenuPosition()}.
     * @return this instance for method chaining.
     */
    AppAction setInMenuBar(@NotNull AppMainMenuType menu, double position);

    /**
     * @return {@code true} if this action should be added to the main menu.
     */
    boolean isInMenuBar();

    /**
     * @return {@code true} if this component {@link #isInMenuBar() is in main menu} and
     *         it does not belong in a sub-menu within that menu.
     */
    boolean isTopLevelMenuItem();

    /**
     * @return Position value indicating where the exported menu item sits relative to the
     * other menu items in the same popup. The value is usually between 0.0 - 1.0d,
     * where 0.0 indicates the top of the popup.
     */
    double getMenuPosition();

    /**
     * This value is only relevant if the action {@link #isInMenuBar() is in main menu}.
     *
     * @return Main menu category in which this action will export its menu item under.
     * @see #setInMenuBar(AppMainMenuType, double)
     */
    @NotNull AppMainMenuType getMainMenuType();

    /**
     * Exports this action as a {@link MenuItem}.
     *
     * @return Main menu item component for this action.
     */
    @NotNull MenuItem getAsMenuItem();

    /**
     * Exports this action as a JavaFx {@link Node} component. It is not guaranteed that
     * all actions support this feature, so the result may be null.
     *
     * @return {@link Node} component for this action, may be null.
     */
    @Nullable Node getAsComponent();

    /**
     * Sets the enabled-ness of this action. The state will be updated immediately
     * on all existing exported components.
     *
     * @param enabled true if enabled.
     * @return this instance for method chaining.
     */
    AppAction setEnabled(boolean enabled);

    /**
     * @return Enabled-ness status of this action.
     */
    boolean isEnabled();

    /**
     * Sets the visibility of the exported components for this action.
     *
     * @param visible true if visible.
     * @return this instance for method chaining.
     */
    AppAction setVisible(boolean visible);

    /**
     * @return Visibility status for exported components.
     */
    boolean isVisible();

    /**
     * Marks this action as having been added to the main menu already, so that
     * it can be ignored if it is to be added again.
     * <p/>
     * @apiNote This is more of an implementation detail for {@link AppSubMenuAction}.
     */
    void markAsAddedToMenu();

    /**
     * @return true if the menu item component for this action has been added to main menu
     * already.
     */
    boolean isAddedToMenu();

    /**
     * Executes the task represented by this action.
     */
    void performAction();

    /**
     * Requests this action to re-fresh its state because a major state change has
     * occurred within {@link AppWindow}.
     *
     * @param context Helper class supplying the context in which this event was triggered.
     */
    void refreshState(AppActionContext context);

    /**
     * Optionally associate an object with this action. Use cases for this include
     * assigning an enum value to identify this action within a list.
     *
     * @param userObject Optional user object.
     * @see #getUserObject()
     */
    void setUserObject(@Nullable Object userObject);

    /**
     * If a user object is not set, returns {@link Optional#empty()}.
     *
     * @return User object stored within this action.
     * @see #setUserObject(Object)
     */
    Optional<Object> getUserObject();

    /**
     * Requests that the component of this action take up as little space as possible.
     *
     * @param compact true to make component compact.
     */
    void setComponentCompact(boolean compact);

    /**
     * @return {@code true} if the component is in compact mode.
     * @see #setComponentCompact(boolean)
     */
    boolean isComponentCompact();

    /**
     * @return The {@link Accelerator} for this action.
     */
    Optional<Accelerator> getAccelerator();
}
