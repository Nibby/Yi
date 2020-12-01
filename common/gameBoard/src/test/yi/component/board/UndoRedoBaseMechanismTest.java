package yi.component.board;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yi.component.board.edits.UndoableEdit;
import yi.models.go.GameModel;
import yi.models.go.StandardGameRules;

/**
 * Tests the correctness of the undo/redo framework. This test suite does focus on
 * model or board state correctness. Individual edit modes must supply their own
 * coverage of undo/redo correctness test to check for that.
 */
public final class UndoRedoBaseMechanismTest {

    private static final class TestEdit extends UndoableEdit {

        @Override
        protected boolean rollbackEditNow(GameModel gameModel) {
            return true; // Do nothing
        }

        @Override
        protected boolean performEditNow(GameModel gameModel) {
            return true; // Do nothing
        }

        @Override
        protected boolean canRollback() {
            return true;
        }
    }

    @Test
    public void testActionRecordedToHistory() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = new GameBoardManager();
        manager.setGameModel(model);

        var editor = new GameModelEditor();
        editor.setMaxHistorySize(10);

        Assertions.assertEquals(0, editor.getCurrentHistorySize());

        // Method under test
        editor.recordAndApply(new TestEdit(), manager);

        // Assert
        Assertions.assertEquals(1, editor.getCurrentHistorySize());
    }

    @Test
    public void testHistoryPrunedToMaxSize() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = new GameBoardManager();
        manager.setGameModel(model);

        var editor = new GameModelEditor();
        editor.setMaxHistorySize(3);

        Assertions.assertEquals(0, editor.getCurrentHistorySize());

        var item1 = new TestEdit();
        var item2 = new TestEdit();
        var item3 = new TestEdit();
        var item4 = new TestEdit();

        // Method under test
        editor.recordAndApply(item1, manager);
        editor.recordAndApply(item2, manager);
        editor.recordAndApply(item3, manager);
        editor.recordAndApply(item4, manager); // Exceeds max history size, pushes out (0, 0) from undo history stack

        Assertions.assertEquals(3, editor.getCurrentHistorySize());
        Assertions.assertEquals(item2, editor.getHistoryItem(0), "Incorrect undo stack state. item2 is not the bottom-most item.");
        Assertions.assertEquals(item3, editor.getHistoryItem(1), "Incorrect undo stack state. item3 is not the middle item.");
        Assertions.assertEquals(item4, editor.getHistoryItem(2), "Incorrect undo stack state. item4 is not the top item.");
    }

    @Test
    public void testHistoryPrunedAfterSettingNewMaxSize() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = new GameBoardManager();
        manager.setGameModel(model);

        var editor = new GameModelEditor();
        editor.setMaxHistorySize(4);

        Assertions.assertEquals(0, editor.getCurrentHistorySize());

        var item1 = new TestEdit();
        var item2 = new TestEdit();
        var item3 = new TestEdit();
        var item4 = new TestEdit();

        editor.recordAndApply(item1, manager);
        editor.recordAndApply(item2, manager);
        editor.recordAndApply(item3, manager);
        editor.recordAndApply(item4, manager);

        // Method under test. Keeps top two items and discards the bottom 2.
        editor.setMaxHistorySize(2);

        Assertions.assertEquals(2, editor.getCurrentHistorySize());
        Assertions.assertEquals(item3, editor.getHistoryItem(0), "Incorrect undo stack state. item3 is not the bottom item.");
        Assertions.assertEquals(item4, editor.getHistoryItem(1), "Incorrect undo stack state. item4 is not the top item.");
    }

    @Test
    public void testCanUndo_InEmptyHistory_IsFalse() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = new GameBoardManager();
        manager.setGameModel(model);

        var editor = new GameModelEditor();
        editor.setMaxHistorySize(4);

        Assertions.assertFalse(editor.canUndo());
    }

    @Test
    public void testCanRedo_InEmptyHistory_IsFalse() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = new GameBoardManager();
        manager.setGameModel(model);

        var editor = new GameModelEditor();
        editor.setMaxHistorySize(4);

        Assertions.assertFalse(editor.canRedo());
    }

    @Test
    public void testHasHistory_UndoConsecutively_StateCorrect() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = new GameBoardManager();
        manager.setGameModel(model);

        var editor = new GameModelEditor();
        editor.setMaxHistorySize(4);

        Assertions.assertEquals(0, editor.getCurrentHistorySize());

        var item1 = new TestEdit();
        var item2 = new TestEdit();
        var item3 = new TestEdit();
        var item4 = new TestEdit();

        editor.recordAndApply(item1, manager);
        editor.recordAndApply(item2, manager);
        editor.recordAndApply(item3, manager);
        editor.recordAndApply(item4, manager);

        var originalHistorySize = editor.getCurrentHistorySize();

        // Method under test
        editor.performUndo(manager);
        runStateAssertions(originalHistorySize, 2, true, true, false, editor);

        editor.performUndo(manager);
        runStateAssertions(originalHistorySize, 1, true, true, false, editor);

        editor.performUndo(manager);
        runStateAssertions(originalHistorySize, 0, true, true, false, editor);

        editor.performUndo(manager);
        runStateAssertions(originalHistorySize, -1, true, false, false, editor);
    }

    @Test
    public void testHasHistory_UndoConsecutivelyThenPerformNewEdit_DiscardsOldEditsBeyondCurrentPosition() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = new GameBoardManager();
        manager.setGameModel(model);

        var editor = new GameModelEditor();
        editor.setMaxHistorySize(4);

        Assertions.assertEquals(0, editor.getCurrentHistorySize());

        var item1 = new TestEdit();
        var item2 = new TestEdit();
        var item3 = new TestEdit();
        var item4 = new TestEdit();

        editor.recordAndApply(item1, manager);
        editor.recordAndApply(item2, manager);
        editor.recordAndApply(item3, manager);
        editor.recordAndApply(item4, manager);

        var originalHistorySize = 4;

        editor.performUndo(manager);
        runStateAssertions(originalHistorySize, 2, true, true, false, editor);

        editor.performUndo(manager);
        runStateAssertions(originalHistorySize, 1, true, true, false, editor);

        // Method under test
        editor.recordAndApply(new TestEdit(), manager);
        runStateAssertions(3, 2, false, true, false, editor);
    }

    @Test
    public void testHasHistory_UndoConsecutivelyThenPerformIdenticalEditAtThatPosition_DiscardsOldEditsAnyway() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = new GameBoardManager();
        manager.setGameModel(model);

        var editor = new GameModelEditor();
        editor.setMaxHistorySize(4);

        Assertions.assertEquals(0, editor.getCurrentHistorySize());

        var item1 = new TestEdit();
        var item2 = new TestEdit();
        var item3 = new TestEdit();
        var item4 = new TestEdit();

        editor.recordAndApply(item1, manager);
        editor.recordAndApply(item2, manager);
        editor.recordAndApply(item3, manager);
        editor.recordAndApply(item4, manager);

        var originalHistorySize = 4;

        editor.performUndo(manager);
        runStateAssertions(originalHistorySize, 2, true, true, false, editor);

        editor.performUndo(manager);
        runStateAssertions(originalHistorySize, 1, true, true, false, editor);

        // Method under test
        editor.recordAndApply(item3, manager);
        runStateAssertions(3, 2, false, true, true, editor);
    }

    @Test
    public void testHasHistory_UndoConsecutivelyThenPerformRedo_StateCorrect() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = new GameBoardManager();
        manager.setGameModel(model);

        var editor = new GameModelEditor();
        editor.setMaxHistorySize(4);

        Assertions.assertEquals(0, editor.getCurrentHistorySize());

        var item1 = new TestEdit();
        var item2 = new TestEdit();
        var item3 = new TestEdit();
        var item4 = new TestEdit();

        editor.recordAndApply(item1, manager);
        editor.recordAndApply(item2, manager);
        editor.recordAndApply(item3, manager);
        editor.recordAndApply(item4, manager);

        var originalHistorySize = 4;

        editor.performUndo(manager);
        runStateAssertions(originalHistorySize, 2, true, true, false, editor);

        editor.performUndo(manager);
        runStateAssertions(originalHistorySize, 1, true, true, false, editor);

        // Method under test
        editor.performRedo(manager);
        runStateAssertions(originalHistorySize, 2, true, true, true, editor);

        editor.performRedo(manager);
        runStateAssertions(originalHistorySize, 3, false, true, true, editor);
    }

    private void runStateAssertions(int expectedHistorySize, int expectedPositionInHistory, boolean canRedo, boolean canUndo,
                                    boolean redoRatherThanUndo, GameModelEditor editor) {
        Assertions.assertEquals(expectedHistorySize, editor.getCurrentHistorySize(), redoRatherThanUndo ? "Redo" : "Undo" + " caused history size to change.");
        Assertions.assertEquals(expectedPositionInHistory, editor.getCurrentPositionInHistory(), "Unexpected history in position.");
        Assertions.assertEquals(canRedo, editor.canRedo(), "Unexpected redo-ability state.");
        Assertions.assertEquals(canUndo, editor.canUndo(), "Unexpected undo-ability state.");
    }
}
