package yi.editor.framework.action;

import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import yi.editor.EditorUITestBase;
import yi.editor.components.EditorMainMenuType;
import yi.editor.framework.EditorTextResources;

/**
 * Tests the selection state of {@link EditorToggleAction} is behaving correctly
 * on the action itself and its exported components.
 */
public final class EditorToggleActionTest extends EditorUITestBase {

    private EditorToggleAction action1;
    private EditorToggleAction action2;
    private EditorToggleAction action3;

    @Start
    @Override
    public void startFx(Stage stage) {
        super.startFx(stage);
    }

    @Override
    protected void initializeTestActions(EditorActionManager actionManager) {
        action1 = createTestToggleAction();
        action1.setInMenuBar(EditorMainMenuType.TESTING, 0d);

        action2 = createTestToggleAction();
        action2.setInMenuBar(EditorMainMenuType.TESTING, 1d);

        action3 = createTestToggleAction();
        action3.setInMenuBar(EditorMainMenuType.TESTING, 0.5d);

        action1.setSelected(true);

        actionManager.addActions(new EditorAction[]{ action1, action2, action3 });
    }

    @Test
    public void testSelectionStateCorrect() {
        testAction();
        testMenuItem();
    }

    private void testAction() {
        assertActionStateCorrect(true, false, false);

        action2.setSelected(true);
        assertActionStateCorrect(true, true, false);

        action3.setSelected(true);
        assertActionStateCorrect(true, true, true);

        action1.setSelected(false);
        assertActionStateCorrect(false, true, true);
    }

    private void testMenuItem() {
        assertMenuItemStateCorrect(false, true, true);

        action2.setSelected(false);
        assertMenuItemStateCorrect(false, false, true);

        action3.setSelected(false);
        assertMenuItemStateCorrect(false, false, false);

        action1.setSelected(true);
        assertMenuItemStateCorrect(true, false, false);
    }

    private void assertMenuItemStateCorrect(boolean action1Selected,
                                            boolean action2Selected,
                                            boolean action3Selected) {
        Assertions.assertEquals(action1Selected, action1.getAsMenuItem().isSelected());
        Assertions.assertEquals(action2Selected, action2.getAsMenuItem().isSelected());
        Assertions.assertEquals(action3Selected, action3.getAsMenuItem().isSelected());
    }

    private void assertActionStateCorrect(boolean action1Selected,
                                          boolean action2Selected,
                                          boolean action3Selected) {
        Assertions.assertEquals(action1Selected, action1.isSelected());
        Assertions.assertEquals(action2Selected, action2.isSelected());
        Assertions.assertEquals(action3Selected, action3.isSelected());
    }

    private EditorToggleAction createTestToggleAction() {
        return new EditorToggleAction(EditorTextResources.EMPTY, null);
    }

}
