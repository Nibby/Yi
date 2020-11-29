package yi.editor.framework.action;

import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import yi.editor.EditorMainMenuType;
import yi.editor.EditorTextResources;
import yi.editor.EditorUITestBase;

public class EditorRadioActionTest extends EditorUITestBase {

    @Start
    @Override
    public void startFx(Stage stage) {
        super.startFx(stage);
    }

    private final ToggleGroup toggleGroup = new ToggleGroup();
    private EditorRadioAction action1;
    private EditorRadioAction action2;
    private EditorRadioAction action3;

    @Override
    protected void performTasksBeforeCreatingFrame() {
        action1 = createTestRadioAction();
        action1.setInMainMenu(EditorMainMenuType.TESTING, 0d);
        action1.setMenuToggleGroup(toggleGroup);

        action2 = createTestRadioAction();
        action2.setInMainMenu(EditorMainMenuType.TESTING, 1d);
        action2.setMenuToggleGroup(toggleGroup);

        action3 = createTestRadioAction();
        action3.setInMainMenu(EditorMainMenuType.TESTING, 0.5d);
        action3.setMenuToggleGroup(toggleGroup);

        action1.setSelected(true);
    }

    @Test
    public void testAction_SelectionStateCorrect() {
        assertActionStateCorrect(true, false, false);

        action2.setSelected(true);
        assertActionStateCorrect(false, true, false);

        action3.setSelected(true);
        assertActionStateCorrect(false, false, true);

        action1.setSelected(true);
        assertActionStateCorrect(true, false, false);
    }

    @Test
    public void testMenuItem_SelectionStateCorrect() {
        assertMenuItemStateCorrect(true, false, false);

        action2.setSelected(true);
        assertMenuItemStateCorrect(false, true, false);

        action3.setSelected(true);
        assertMenuItemStateCorrect(false, false, true);

        action1.setSelected(true);
        assertMenuItemStateCorrect(true, false, false);
    }

//    @Test
//    public void testNode_SelectionStateCorrect() {
//        // TODO: Implement this later when we need to export actions to nodes
//    }

    private void assertMenuItemStateCorrect(boolean action1Selected,
                                            boolean action2Selected,
                                            boolean action3Selected) {
        Assertions.assertEquals(action1Selected, ((RadioMenuItem) action1.getAsMenuItem()).isSelected());
        Assertions.assertEquals(action2Selected, ((RadioMenuItem) action2.getAsMenuItem()).isSelected());
        Assertions.assertEquals(action3Selected, ((RadioMenuItem) action3.getAsMenuItem()).isSelected());
    }

    private void assertActionStateCorrect(boolean action1Selected,
                                          boolean action2Selected,
                                          boolean action3Selected) {
        Assertions.assertEquals(action1Selected, action1.isSelected());
        Assertions.assertEquals(action2Selected, action2.isSelected());
        Assertions.assertEquals(action3Selected, action3.isSelected());
    }

    private EditorRadioAction createTestRadioAction() {
        return new EditorRadioAction(EditorTextResources.EMPTY, null);
    }
}
