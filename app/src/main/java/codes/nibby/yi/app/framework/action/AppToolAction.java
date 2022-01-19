package codes.nibby.yi.app.framework.action;

import codes.nibby.yi.app.components.AppEditTool;
import codes.nibby.yi.app.components.AppMainMenuType;
import codes.nibby.yi.app.components.board.GameBoardViewer;
import codes.nibby.yi.app.components.board.editmodes.AbstractEditMode;
import codes.nibby.yi.app.framework.*;
import codes.nibby.yi.app.framework.property.BooleanProperty;
import codes.nibby.yi.app.framework.property.NullableProperty;
import codes.nibby.yi.app.i18n.TextResource;
import codes.nibby.yi.models.editor.GameModelEditor;
import javafx.scene.control.ToggleGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public class AppToolAction extends AppAbstractAction<YiRadioMenuItem, YiToggleButton> {

    private final NullableProperty<ToggleGroup> componentGroup = new NullableProperty<>(null);
    private final NullableProperty<ToggleGroup> menuItemGroup = new NullableProperty<>(null);

    private final BooleanProperty selectedProperty = new BooleanProperty(false);

    private AppToolAction(@NotNull TextResource name) {
        super(name, null);

        selectedProperty.addListener(this::updateSelectionState);

        componentGroup.addListener(newValue ->
            getCachedComponent().ifPresent(comp -> comp.setToggleGroup(newValue))
        );

        menuItemGroup.addListener(newValue ->
            getCachedMenuItem().ifPresent(menuItem -> menuItem.setToggleGroup(newValue))
        );
    }

    private void updateSelectionState(boolean isSelectedNow) {
        getCachedMenuItem().ifPresent(item -> {
            if (isSelectedNow != item.isSelected()) {
                item.setSelected(isSelectedNow);
            }
        });
        getCachedComponent().ifPresent(component -> {
            if (isSelectedNow != component.isSelected()) {
                component.setSelected(isSelectedNow);
            }
        });
    }

    @Override
    protected @NotNull YiRadioMenuItem getAsMenuItemImpl() {
        var item = new YiRadioMenuItem(getText(), getIcon());
        item.selectedProperty().addListener(event -> setSelected(item.isSelected()));
        getMenuItemToggleGroup().ifPresent(item::setToggleGroup);
        item.setSelected(isSelected());
        return item;
    }

    @Override
    protected @Nullable YiToggleButton getAsComponentImpl() {
        var toggleButton = new YiToggleButton(getText(), getIcon());
        toggleButton.selectedProperty().addListener(event -> setSelected(toggleButton.isSelected()));
        getComponentToggleGroup().ifPresent(toggleButton::setToggleGroup);
        toggleButton.setSelected(isSelected());
        return toggleButton;
    }

    /**
     * Sets the selected property for this action, which will also update the selected
     * state on exported components. If this action belongs in a toggle group, other
     * radio components will become de-selected and fire a de-selection event.
     *
     * @param isSelected true if the action is selected.
     */
    public void setSelected(boolean isSelected) {
        selectedProperty.set(isSelected);
    }

    /**
     * @return true if this action is currently selected.
     */
    public boolean isSelected() {
        return selectedProperty.get();
    }

    /**
     * Set a toggle group for the component of this action created using
     * {@link #getAsComponent()}. Once set, only one item in the group may be
     * selected at any time. The parameter may be null, in which case the component will
     * not be set to any group.
     * <p/>
     * Note that this method only applies to the component, not the menu item. Use
     * {@link #setMenuToggleGroup(ToggleGroup)} to assign toggle groups to menu items.
     *
     * @param toggleGroup Component toggle group.
     */
    public void setComponentToggleGroup(@Nullable ToggleGroup toggleGroup) {
        this.componentGroup.set(toggleGroup);
    }

    /**
     * @return Toggle group the component for this action currently belongs to.
     * @see #setComponentToggleGroup(ToggleGroup)
     */
    public Optional<ToggleGroup> getComponentToggleGroup() {
        return componentGroup.get();
    }

    /**
     * Set a toggle group for the menu item of this action created using
     * {@link #getAsMenuItem()}. Once set, only one item in the group may be
     * selected at any time. The parameter may be null, in which case the menu item will
     * not be set to any group.
     * <p/>
     * Note that this method only applies to the menu item, not the component. Use
     * {@link #setComponentToggleGroup(ToggleGroup)} to assign toggle groups to components.
     *
     * @param toggleGroup Menu item toggle group.
     */
    public void setMenuToggleGroup(@Nullable ToggleGroup toggleGroup) {
        this.menuItemGroup.set(toggleGroup);
    }

    /**
     * @return Toggle group the menu item for this action currently belongs to.
     * @see #setMenuToggleGroup(ToggleGroup)
     */
    public Optional<ToggleGroup> getMenuItemToggleGroup() {
        return menuItemGroup.get();
    }

    public static AppToolAction forTool(
        final AppEditTool tool,
        final ToggleGroup componentGroup,
        final ToggleGroup menuGroup,
        TextResource label,
        @Nullable AppIcon icon,
        @Nullable AppAccelerator acceleratorId,
        Predicate<AbstractEditMode> selectionCriteria,
        double menuPosition
    ) {
        var action = new AppToolAction(label) {
            @Override
            public void refreshState(AppActionContext context) {
                super.refreshState(context);
                var boardViewer = getGameBoardViewer(context);
                var mode = boardViewer.getEditMode();
                var selected = selectionCriteria.test(mode);
                setSelected(selected);
            }
        };
        action.setAction(context -> {
            AppWindow window = context.getInvokerWindow();
            GameBoardViewer board = window.getBoardArea().getGameBoardViewer();
            GameModelEditor editor = window.getGameModel().getEditor();

            if (action.isSelected()) {
                tool.apply(getGameBoardViewer(context));
                editor.setEditable(true);
            } else if (componentGroup.getSelectedToggle() == null && board.isEditable()) {
                editor.setEditable(false);
            }
        });
        action.setInMenuBar(AppMainMenuType.TOOLS, menuPosition);
        if (acceleratorId != null) {
            action.setAccelerator(acceleratorId);
        }

        action.setShowIconOnMenuItem(false);
        action.setShowIconOnComponent(true);

        YiToggleButton actionComponent = action.getAsComponent();
        assert actionComponent != null : "Editor tool action component must not be null";

        if (icon != null) {
            AppIcon unselectedStateIcon = icon.getDarkModeIcon();
            AppIcon selectedStateIcon = icon.getLightModeIcon();

            action.setIcon(unselectedStateIcon);

            actionComponent.selectedProperty().addListener(event -> {
                boolean selectedNow = actionComponent.isSelected();
                AppIcon iconToUse = selectedNow ? selectedStateIcon : unselectedStateIcon;
                action.setIcon(iconToUse);
            });
        }
        action.setComponentToggleGroup(componentGroup);
        action.setMenuToggleGroup(menuGroup);

        return action;
    }

    private static GameBoardViewer getGameBoardViewer(AppActionContext context) {
        var window = context.getInvokerWindow();
        var board  = window.getBoardArea();
        return board.getGameBoardViewer();
    }
}
