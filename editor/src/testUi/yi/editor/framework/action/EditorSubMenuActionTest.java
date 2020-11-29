package yi.editor.framework.action;

import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import yi.editor.EditorMainMenuType;
import yi.editor.EditorTextResources;
import yi.editor.EditorUITestBase;

/**
 * Tests the sub-menu hierarchy is constructed correctly.
 */
public class EditorSubMenuActionTest extends EditorUITestBase {

    @Start
    @Override
    public void startFx(Stage stage) {
        super.startFx(stage);
    }

    private EditorSubMenuAction subMenu1;
    private EditorAction itemInSubMenu1;

    private EditorSubMenuAction subMenu2;
    private EditorAction itemInSubMenu2;

    @Override
    protected void initializeTestActions(EditorActionManager manager) {
        subMenu1 = new EditorSubMenuAction(manager, EditorTextResources.MENUITEM_NEW_GAME)
                        .setInMainMenu(EditorMainMenuType.TESTING, 0d);
        itemInSubMenu1 = new EditorBasicAction(manager, EditorTextResources.MENU_PERSPECTIVE, null)
                        .setInMainMenu(EditorMainMenuType.TESTING, 0d);
        subMenu1.addChildAction(itemInSubMenu1);

        subMenu2 = new EditorSubMenuAction(manager, EditorTextResources.MENUITEM_SAVE_AS_GAME)
                .setInMainMenu(EditorMainMenuType.TESTING, 1d);
        itemInSubMenu2 = new EditorBasicAction(manager, EditorTextResources.MENUITEM_SAVE_GAME, null)
                .setInMainMenu(EditorMainMenuType.TESTING, 1d);
        subMenu2.addChildAction(itemInSubMenu2);
        subMenu1.addChildAction(subMenu2);

        /*
            We should have a hierarchy that looks like:

            subMenu1 >  itemInSubMenu1
                            subMenu2   >  itemInSubMenu2
         */
    }

    @Test
    public void testSubMenu_ConstructedCorrectlyInMenuBar() {
        Menu testingMenu = getMenu(EditorMainMenuType.TESTING).orElseThrow();

        ObservableList<MenuItem> layer1Items = testingMenu.getItems();
        Assertions.assertEquals(1, layer1Items.size(),
                "Only subMenu1 should appear in first layer, got " + layer1Items.size() + " items");
        Assertions.assertEquals(subMenu1.getAsMenuItem(), layer1Items.get(0),
                "The only item in first layer is not subMenu1's component " +
                        "(or at least .equals() returned false)");

        MenuItem theOnlyLayer1Item = layer1Items.get(0);
        Assertions.assertTrue(theOnlyLayer1Item instanceof Menu,
                "subMenu1 exported a menu component that is not of type Menu");

        ObservableList<MenuItem> layer2Items = ((Menu) theOnlyLayer1Item).getItems();
        Assertions.assertEquals(2, layer2Items.size(),
                "itemInSubMenu1 and subMenu2 appears in layer 2, in that order. " +
                        "Actual number of menu items in this layer is " + layer2Items.size());
        Assertions.assertEquals(itemInSubMenu1.getAsMenuItem(), layer2Items.get(0),
                "First item in layer2 is not itemInSubMenu1 (is it ordered correctly?)");
        Assertions.assertEquals(subMenu2.getAsMenuItem(), layer2Items.get(1),
                "Second item in layer2 is not subMenu2  (is it ordered correctly?)");

        MenuItem layer2SubMenu = layer2Items.get(1);
        Assertions.assertTrue(layer2SubMenu instanceof Menu,
                "Second item in layer 2 is not an instance of Menu");

        ObservableList<MenuItem> layer3Items = ((Menu) layer2SubMenu).getItems();
        Assertions.assertEquals(1, layer3Items.size(),
                "Only itemInSubMenu2 should appear in layer 3, got " + layer3Items.size()
                        + " components");
        Assertions.assertEquals(itemInSubMenu2.getAsMenuItem(), layer3Items.get(0),
                "Item in layer 3 is not the same item exported by itemInSubMenu2, " +
                        "or at least equals() returned false.");
    }
}
