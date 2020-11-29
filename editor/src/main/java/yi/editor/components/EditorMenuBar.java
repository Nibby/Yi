package yi.editor.components;

import javafx.scene.control.MenuBar;
import javafx.scene.control.ToggleGroup;
import yi.common.utilities.SystemUtilities;
import yi.component.YiCheckMenuItem;
import yi.component.YiMenu;
import yi.component.YiRadioMenuItem;
import yi.editor.EditorFrame;
import yi.editor.EditorHelper;
import yi.editor.EditorMainMenuType;
import yi.editor.EditorTextResources;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.accelerator.EditorAcceleratorManager;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.settings.EditorSettings;

import java.util.*;

import static yi.editor.EditorTextResources.MENU_PERSPECTIVE;
import static yi.editor.EditorTextResources.MENU_VIEW;

/**
 * Primary menu bar for {@link yi.editor.EditorFrame}.
 */
public class EditorMenuBar extends MenuBar {

    public EditorMenuBar(EditorActionManager actionManager) {
        var actionHelper = actionManager.getContext();

        Set<EditorAction> allActions = actionManager.getAllActions();
        var topLevelActionsByMenu = new HashMap<EditorMainMenuType, List<EditorAction>>();

        for (EditorAction action : allActions) {
            if (action.isInMainMenu()) {
                var menuType = action.getMainMenuType();
                topLevelActionsByMenu.putIfAbsent(menuType, new ArrayList<>());

                var menuItem = action.getAsMenuItem();
                menuItem.setOnAction(e -> action.performAction(actionHelper));

                if (action.isTopLevelMenuItem()) {
                    // Non top-level items will be added by
                    // EditorSubMenuAction recursively when we
                    // call getAsMenuItem()
                    //
                    // I still don't think this is ideal
                    // because it relies on implementation detail...
                    topLevelActionsByMenu.get(menuType).add(action);
                }
            }
        }

        var mainMenuOrdered = new ArrayList<>(topLevelActionsByMenu.keySet());
        mainMenuOrdered.sort(Comparator.comparingInt(EditorMainMenuType::getOrder));

        for (EditorMainMenuType menuType : mainMenuOrdered) {
            var menu = new YiMenu(menuType.getName());
            menu.setUserData(menuType);
            menu.setVisible(menuType.isVisible());
            getMenus().add(menu);

            topLevelActionsByMenu.get(menuType)
                    .sort(Comparator.comparingDouble(EditorAction::getMenuPosition));

            for (EditorAction action : topLevelActionsByMenu.get(menuType)) {
                menu.getItems().add(action.getAsMenuItem());
            }
        }

        if (SystemUtilities.isMac()) {
            setUseSystemMenuBar(EditorHelper.isUsingSystemMenuBar());
        }
    }

    private YiMenu createViewMenu(EditorFrame frame) {
        var viewMenu = new YiMenu(MENU_VIEW);
        {
            var currentLayout = frame.getPerspective();
            var radioGroup = new ToggleGroup();
            var perspectiveMenu = new YiMenu(MENU_PERSPECTIVE);
            var layoutToItemMap = new HashMap<EditorPerspective, YiRadioMenuItem>();

            for (EditorPerspective layout : EditorPerspective.values()) {
                var menuItem = new YiRadioMenuItem(layout.getFriendlyName());
                menuItem.setOnAction(e -> frame.setPerspective(layout));
                menuItem.setSelected(layout == currentLayout);

                var acceleratorId = layout.getAcceleratorId();
                EditorAcceleratorManager.install(acceleratorId, menuItem);

                radioGroup.getToggles().add(menuItem);
                perspectiveMenu.getItems().add(menuItem);
                layoutToItemMap.put(layout, menuItem);
            }

            frame.addContentLayoutChangeListener(newLayout -> {
                var menuItemToSelect = layoutToItemMap.get(newLayout);
                if (menuItemToSelect == null) {
                    // Only reason I can think of for this to happen is when Java GC'd
                    // the HashMap storing this. I think the solution to that might be
                    // to elevate the map to a private field variable...
                    throw new IllegalStateException("Cannot find matching menu item to " +
                            "select for layout: " + newLayout.name());
                }
                menuItemToSelect.setSelected(true);
            });

            viewMenu.getItems().add(perspectiveMenu);
        }

        { // Toggle coordinates
            var toggleCoordinates = new YiCheckMenuItem(EditorTextResources.MENUITEM_TOGGLE_COORDINATES);
            toggleCoordinates.setSelected(EditorSettings.general.isShowingBoardCoordinates());
            toggleCoordinates.setOnAction(e -> {
                var showIt = toggleCoordinates.isSelected();
                frame.getBoardArea().getGameBoardViewer().setShowCoordinates(showIt);
                EditorSettings.general.setShowBoardCoordinates(showIt);
            });
            EditorAcceleratorManager.getAccelerator(EditorAcceleratorId.TOGGLE_BOARD_COORDINATES).install(toggleCoordinates);
            viewMenu.getItems().add(toggleCoordinates);
        }

        return viewMenu;
    }
}
