package yi.editor.components;

import javafx.scene.control.MenuBar;
import yi.common.utilities.SystemUtilities;
import yi.component.YiMenu;
import yi.editor.EditorHelper;
import yi.editor.EditorMainMenuType;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionManager;

import java.util.*;

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
}
