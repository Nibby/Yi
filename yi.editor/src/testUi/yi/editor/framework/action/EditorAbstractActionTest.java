package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import yi.component.shared.component.YiButton;
import yi.component.shared.component.YiMenuItem;
import yi.component.shared.utilities.GuiUtilities;
import yi.editor.EditorUITestBase;
import yi.editor.components.EditorMainMenuType;
import yi.editor.components.EditorTextResources;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.accelerator.EditorAcceleratorManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests the menu items are exported correctly and responding correctly to changes to
 * the action state.
 */
public final class EditorAbstractActionTest extends EditorUITestBase {

    private EditorAction action1;
    private EditorAction action2;
    private EditorAction action3;

    private final AtomicBoolean action1ActionPerformed = new AtomicBoolean(false);
    private final AtomicBoolean action2ActionPerformed = new AtomicBoolean(false);
    private final AtomicBoolean action3ActionPerformed = new AtomicBoolean(false);

    @Start
    @Override
    public void startFx(Stage stage) {
        super.startFx(stage);
    }

    @Override
    protected void initializeTestActions(EditorActionManager actionManager) {
        action1 = createAction(() -> action1ActionPerformed.set(true));
        action1.setInMainMenu(EditorMainMenuType.TESTING, 0d);

        action2 = createAction(() -> action2ActionPerformed.set(true));
        action2.setInMainMenu(EditorMainMenuType.TESTING, 1d);

        action3 = createAction(() -> action3ActionPerformed.set(true));
        action3.setInMainMenu(EditorMainMenuType.TESTING, 0.5d);

        actionManager.addActions(new EditorAction[] { action1, action2, action3 });
    }

    @Test
    public void testBaseImplementationCorrect() {
        var menuItemMap = new HashMap<EditorAction, MenuItem>();
        var nodeMap = new HashMap<EditorAction, Node>();

        menuItemMap.put(action1, action1.getAsMenuItem());
        menuItemMap.put(action2, action2.getAsMenuItem());
        menuItemMap.put(action3, action3.getAsMenuItem());

        nodeMap.put(action1, action1.getAsComponent());
        nodeMap.put(action2, action2.getAsComponent());
        nodeMap.put(action3, action3.getAsComponent());

        testPositionCorrect();
        testSetVisibility(menuItemMap, nodeMap);
        testSetEnabled(menuItemMap, nodeMap);
        testSetName(menuItemMap, nodeMap);
        testSetIcon(menuItemMap, nodeMap);
        testSetAccelerator(menuItemMap);
        testActionPerformed();
    }

    private void testActionPerformed() {
        var mainMenuBar = window.getMainMenuBar();
        Menu testingMenu = mainMenuBar.getMenus().stream()
                .filter(menu -> menu.getUserData() == EditorMainMenuType.TESTING)
                .findFirst()
                .orElseThrow();

        // Sanity check
        Assertions.assertFalse(action1ActionPerformed.get());
        Assertions.assertFalse(action2ActionPerformed.get());
        Assertions.assertFalse(action3ActionPerformed.get());

        for (MenuItem menuItem : testingMenu.getItems()) {
            var data = menuItem.getUserData();
            if (data instanceof EditorAction) {
                menuItem.fire();
            }
        }

        Assertions.assertTrue(action1ActionPerformed.get());
        Assertions.assertTrue(action2ActionPerformed.get());
        Assertions.assertTrue(action3ActionPerformed.get());
    }

    private void testSetAccelerator(HashMap<EditorAction, MenuItem> menuItemMap) {

        Assertions.assertNull(menuItemMap.get(action1).getAccelerator(),
                "Initial accelerator state not correct");

        var newAcceleratorId = EditorAcceleratorId.NEW_GAME;
        action1.setAccelerator(newAcceleratorId);

        Assertions.assertEquals(EditorAcceleratorManager.getAccelerator(newAcceleratorId).getKeyCombination(),
                menuItemMap.get(action1).getAccelerator(),
                "Accelerator not updated after setAccelerator() call");
    }

    private void testSetIcon(HashMap<EditorAction, MenuItem> menuItemMap,
                             HashMap<EditorAction, Node> nodeMap) {

        action1.setIcon(null);

        Assertions.assertNull(menuItemMap.get(action1).getGraphic(),
                "Initial graphic should be empty on menu item");
        Assertions.assertNull(((Labeled) nodeMap.get(action1)).getGraphic(),
                "Initial graphic should be empty on node");

        // A little yuck, but we have to be careful to use a 16x16 icon here otherwise
        // the action will wrap the image in a re-sized version, causing the two objects
        // to not equal each other.
        ImageView icon = GuiUtilities.getIcon("/testIcon.png", getClass()).orElseThrow();
        action1.setIcon(icon);

        Assertions.assertEquals(menuItemMap.get(action1).getGraphic(), icon,
                "New graphic not updated on menu item");
        Assertions.assertEquals(((Labeled) nodeMap.get(action1)).getGraphic(), icon,
                "New graphic not updated on node");
    }

    private void testSetName(HashMap<EditorAction, MenuItem> menuItemMap,
                             HashMap<EditorAction, Node> nodeMap) {

        Assertions.assertEquals(menuItemMap.get(action1).getText(),
                EditorTextResources.EMPTY.getLocalisedText(),
                "Initial name not correct on menu item");

        Assertions.assertEquals(((Labeled) nodeMap.get(action1)).getText(),
                EditorTextResources.EMPTY.getLocalisedText(),
                "Initial name not correct on node");

        action1.setText(EditorTextResources.MENUITEM_SAVE_GAME);

        Assertions.assertEquals(menuItemMap.get(action1).getText(),
                EditorTextResources.MENUITEM_SAVE_GAME.getLocalisedText(),
                "Adjusted name not reflected on menu item");

        Assertions.assertEquals(((Labeled) nodeMap.get(action1)).getText(),
                EditorTextResources.MENUITEM_SAVE_GAME.getLocalisedText(),
                "Adjusted name not reflected on node");
    }

    private void testSetEnabled(HashMap<EditorAction, MenuItem> menuItemMap,
                                HashMap<EditorAction, Node> nodeMap) {
        Assertions.assertFalse(menuItemMap.get(action1).isDisable(),
                "Initial enabled state not correct on menu item");

        Assertions.assertFalse(nodeMap.get(action1).isDisable(),
                "Initial enabled state not correct on node");

        action1.setEnabled(false);

        Assertions.assertTrue(menuItemMap.get(action1).isDisable(),
                "Enabled state not updated on menu item");
        Assertions.assertTrue(nodeMap.get(action1).isDisable(),
                "Enabled state not updated on node");

        action1.setEnabled(true);

        Assertions.assertFalse(menuItemMap.get(action1).isDisable(),
                "Enabled state not updated on menu item 2nd time");
        Assertions.assertFalse(nodeMap.get(action1).isDisable(),
                "Enabled state not updated on node 2nd time");
    }

    private void testSetVisibility(HashMap<EditorAction, MenuItem> menuItemMap,
                                   HashMap<EditorAction, Node> nodeMap) {

        Assertions.assertTrue(menuItemMap.get(action1).isVisible(),
                "Initial visibility state not correct on menu item");
        Assertions.assertTrue(nodeMap.get(action1).isVisible(),
                "Initial visibility state not correct on node");

        action1.setVisible(false);

        Assertions.assertFalse(menuItemMap.get(action1).isVisible(),
                "Visibility state not updated on menu item");
        Assertions.assertFalse(nodeMap.get(action1).isVisible(),
                "Visibility state not updated on node");

        action1.setVisible(true);

        Assertions.assertTrue(menuItemMap.get(action1).isVisible(),
                "Visibility state not updated on menu item 2nd time");
        Assertions.assertTrue(nodeMap.get(action1).isVisible(),
                "Visibility state not updated on node 2nd time");
    }


    private void testPositionCorrect() {
        var mainMenuBar = window.getMainMenuBar();
        Menu testingMenu = mainMenuBar.getMenus().stream()
                .filter(menu -> menu.getUserData() == EditorMainMenuType.TESTING)
                .findFirst()
                .orElseThrow();

        List<EditorAction> discoveredItems = new ArrayList<>();

        for (MenuItem menuItem : testingMenu.getItems()) {
            var data = menuItem.getUserData();
            if (data instanceof EditorAction) {
                discoveredItems.add((EditorAction) data);
            }
        }

        Assertions.assertEquals(3, discoveredItems.size(),
                "Not all action items are added to menu");
        Assertions.assertEquals(action1, discoveredItems.get(0),
                "First action is not action1, which should be placed on top of the menu");
        Assertions.assertEquals(action3, discoveredItems.get(1),
                "Second action is not action3, which should be placed in the middle of the menu");
        Assertions.assertEquals(action2, discoveredItems.get(2),
                "Third action is not action2, which should be placed on the bottom of the menu");
    }

    private EditorAction createAction(Runnable action) {
        return new EditorAbstractAction<>(EditorTextResources.EMPTY, context -> action.run()) {
            @Override
            protected @NotNull MenuItem getAsMenuItemImpl() {
                return new YiMenuItem(getText());
            }

            @Override
            protected @NotNull Node getAsComponentImpl() {
                return new YiButton(getText());
            }
        };
    }

}
