package codes.nibby.yi.app.components;

import codes.nibby.yi.app.framework.AppWindow;
import codes.nibby.yi.app.framework.YiStyleClass;
import codes.nibby.yi.app.framework.YiToggleButton;
import codes.nibby.yi.app.framework.action.AppAction;
import codes.nibby.yi.app.framework.action.AppSeparatorAction;
import codes.nibby.yi.app.framework.action.AppToolAction;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import codes.nibby.yi.app.framework.property.Property;
import codes.nibby.yi.app.framework.property.PropertyListener;
import codes.nibby.yi.app.utilities.GuiUtilities;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Primary toolbar for {@link AppWindow} that displays a set of supported editing tools
 * and other options.
 */
public final class AppEditToolsToolBar extends ToolBar {

    private final Property<AppEditTool> selectedTool = new Property<>(AppEditTool.PLAY_MOVE);

    private final ToggleGroup toolButtonGroup;
    private final ToggleGroup toolMenuGroup;

    private YiToggleButton toolPlayMove; // Effectively final and non-null because of assertions in constructor
    private final List<AppToolAction> editToolActions;

    public AppEditToolsToolBar() {
        toolButtonGroup = new ToggleGroup();
        toolMenuGroup = new ToggleGroup();

        editToolActions = Arrays.stream(AppEditTool.values())
            .map(value -> {
                var action = value.createAction(toolButtonGroup, toolMenuGroup);
                action.setUserObject(value);
                action.setComponentCompact(true);
                return action;
            })
            .sorted(Comparator.comparingDouble(AppAction::getMenuPosition))
            .collect(Collectors.toList());

        for (AppToolAction action : editToolActions) {
            YiToggleButton button = action.getAsComponent();
            assert button != null : "App tool button shouldn't be null";
            AppEditTool editTool = (AppEditTool) action.getUserObject().orElseThrow();
            button.getStyleClass().add("button-style2");
            button.setFocusTraversable(false);

            if (editTool == AppEditTool.PLAY_MOVE) {
                this.toolPlayMove = button;
            }
        }

        assert this.toolPlayMove != null : "No \"Play Move\" tool was found when constructing toolbar";

        getItems().add(GuiUtilities.createDynamicSpacer());
        addReviewTools();
        getItems().add(GuiUtilities.createDynamicSpacer());

        getStyleClass().add(YiStyleClass.BACKGROUND_BLACK_60_PERCENT.getName());
    }

    public void setContentForLayout(AppPerspective layout) {
        var showIt = layout == AppPerspective.EDIT;
        setVisible(showIt);
        setManaged(showIt);

        if (layout == AppPerspective.COMPACT) {
            toolPlayMove.setSelected(true); // This view is mainly for browsing
        }
    }

    private void addReviewTools() {
        for (AppAction action : AppAction.sorted(getAllActions())) {
            getItems().add(action.getAsComponent());
        }
    }

    public void addSelectedToolChangeListener(PropertyListener<AppEditTool> listener) {
        this.selectedTool.addListener(listener);
    }

    public List<? extends AppAction> getAllActions() {
        var allActions = new ArrayList<AppAction>(editToolActions);
        allActions.addAll(getDividers());
        return allActions;
    }

    private Collection<? extends AppAction> getDividers() {
        var dividers = new ArrayList<AppAction>();
        dividers.add(new AppSeparatorAction().setInMenuBar(AppMainMenuType.TOOLS, 0.005));
        dividers.add(new AppSeparatorAction().setInMenuBar(AppMainMenuType.TOOLS, 0.025));
        dividers.add(new AppSeparatorAction().setInMenuBar(AppMainMenuType.TOOLS, 0.095));
        return dividers;
    }
}
