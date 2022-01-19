package codes.nibby.yi.app.components;

import codes.nibby.yi.app.framework.AppWindow;
import codes.nibby.yi.app.framework.global.GlobalHelper;
import codes.nibby.yi.app.framework.YiMenu;
import codes.nibby.yi.app.framework.action.AppAction;
import codes.nibby.yi.app.framework.action.AppActionContext;
import javafx.scene.control.MenuBar;
import codes.nibby.yi.app.utilities.SystemUtilities;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Primary menu bar for {@link AppWindow}.
 */
public final class AppMenuBar extends MenuBar {

    public AppMenuBar(AppActionContext context, Set<AppAction> allActions) {
        if (SystemUtilities.isMac()) {
            setUseSystemMenuBar(GlobalHelper.isUsingSystemMenuBar());
        }

        var topLevelActionsByMenu = new HashMap<AppMainMenuType, List<AppAction>>();

        for (AppAction action : allActions) {
            action.setContext(context);

            if (action.isInMenuBar()) {
                var menuItem = action.getAsMenuItem();
                menuItem.setOnAction(e -> action.performAction());
                menuItem.setUserData(action);
            }

            if (action.isInMenuBar() && !action.isAddedToMenu()) {
                var menuType = action.getMainMenuType();
                topLevelActionsByMenu.putIfAbsent(menuType, new ArrayList<>());

                if (action.isTopLevelMenuItem()) {
                    // Non top-level items will be added by
                    // EditorSubMenuAction recursively when we
                    // call getAsMenuItem()
                    //
                    // I still don't think this is ideal
                    // because it relies on implementation detail...
                    topLevelActionsByMenu.get(menuType).add(action);
                }
                action.markAsAddedToMenu();
            }
        }

        var mainMenuOrdered = Arrays.stream(AppMainMenuType.values())
                                    .sorted(Comparator.comparingInt(AppMainMenuType::getOrder))
                                    .collect(Collectors.toList());

        for (AppMainMenuType menuType : mainMenuOrdered) {
            var menu = new YiMenu(menuType.getName());
            menu.setUserData(menuType);
            menu.setVisible(menuType.isVisible());
            getMenus().add(menu);

            List<AppAction> actionsInThisMenu = topLevelActionsByMenu.get(menuType);
            if (actionsInThisMenu == null) {
                continue;
            }
            actionsInThisMenu.sort(Comparator.comparingDouble(AppAction::getMenuPosition));
            for (AppAction action : topLevelActionsByMenu.getOrDefault(menuType, new ArrayList<>())) {
                menu.getItems().add(action.getAsMenuItem());
            }
        }
    }
}
