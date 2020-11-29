package yi.editor.framework.action;

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
    public void testMenuItem_SelectionStateCorrect() {
        assertSelectionCorrect(true, false, false);
        action2.setSelected(true);
        assertSelectionCorrect(false, true, false);
        action3.setSelected(true);
        assertSelectionCorrect(false, false, true);
    }

    private void assertSelectionCorrect(boolean action1Selected,
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
