package yi.editor.components;

import javafx.scene.control.MenuBar;
import yi.common.utilities.SystemUtilities;
import yi.component.YiMenu;
import yi.editor.EditorHelper;
import yi.editor.EditorMainMenuType;
import yi.editor.EditorWindow;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Primary menu bar for {@link EditorWindow}.
 */
public class EditorMenuBar extends MenuBar {

    public EditorMenuBar(EditorActionContext context, Set<EditorAction> allActions) {
        if (SystemUtilities.isMac()) {
            setUseSystemMenuBar(EditorHelper.isUsingSystemMenuBar());
        }

        var topLevelActionsByMenu = new HashMap<EditorMainMenuType, List<EditorAction>>();

        for (EditorAction action : allActions) {
            var menuItem = action.getAsMenuItem();
            menuItem.setOnAction(e -> action.performAction(context));
            menuItem.setUserData(action);

            if (action.isInMainMenu() && !action.isAddedToMenu()) {
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

        var mainMenuOrdered = Arrays.stream(EditorMainMenuType.values())
                                    .sorted(Comparator.comparingInt(EditorMainMenuType::getOrder))
                                    .collect(Collectors.toList());

        for (EditorMainMenuType menuType : mainMenuOrdered) {
            var menu = new YiMenu(menuType.getName());
            menu.setUserData(menuType);
            menu.setVisible(menuType.isVisible());
            getMenus().add(menu);

            List<EditorAction> actionsInThisMenu = topLevelActionsByMenu.get(menuType);
            if (actionsInThisMenu == null) {
                continue;
            }
            actionsInThisMenu.sort(Comparator.comparingDouble(EditorAction::getMenuPosition));
            for (EditorAction action : topLevelActionsByMenu.getOrDefault(menuType, new ArrayList<>())) {
                menu.getItems().add(action.getAsMenuItem());
            }
        }
    }
}
