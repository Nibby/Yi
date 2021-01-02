package yi.core.go.editor

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.GameModel
import yi.core.go.StandardGameRules
import yi.core.go.editor.edit.GameModelEdit

class GameModelUndoSystemTest {

    private class TestEdit : GameModelEdit {
        override fun rollbackChanges(model: GameModel) {
        }

        override fun performChanges(model: GameModel) {
        }
    }

    @Test
    fun `Actions are recorded to history properly`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val editor = model.editor
        val undoSystem = editor.undoSystem
        undoSystem.maxHistorySize = 10

        Assertions.assertEquals(0, undoSystem.getEditHistorySize())

        // Method under test
        editor.recordAndApplyUndoable(TestEdit())

        // Assert
        Assertions.assertEquals(1, undoSystem.getEditHistorySize())
    }

    @Test
    fun `Edit size capped at maxHistorySize`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val editor = model.editor
        val undoSystem = editor.undoSystem
        undoSystem.maxHistorySize = 3

        Assertions.assertEquals(0, undoSystem.getEditHistorySize())
        val item1 = TestEdit()
        val item2 = TestEdit()
        val item3 = TestEdit()
        val item4 = TestEdit()

        // Method under test
        editor.recordAndApplyUndoable(item1)
        editor.recordAndApplyUndoable(item2)
        editor.recordAndApplyUndoable(item3)
        editor.recordAndApplyUndoable(item4) // Exceeds max history size, pushes out (0, 0) from undo history stack

        Assertions.assertEquals(3, undoSystem.getEditHistorySize())
        Assertions.assertEquals(item2, undoSystem.getHistoryItem(0),
            "Incorrect undo stack state. item2 is not the bottom-most item.")
        Assertions.assertEquals(item3, undoSystem.getHistoryItem(1),
            "Incorrect undo stack state. item3 is not the middle item.")
        Assertions.assertEquals(item4, undoSystem.getHistoryItem(2),
            "Incorrect undo stack state. item4 is not the top item.")
    }


    @Test
    fun `Edit history cleared after setting new max size`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val editor = model.editor
        val undoSystem = editor.undoSystem
        undoSystem.maxHistorySize = 3

        Assertions.assertEquals(0, undoSystem.getEditHistorySize())
        val item1 = TestEdit()
        val item2 = TestEdit()
        val item3 = TestEdit()

        // Method under test
        editor.recordAndApplyUndoable(item1)
        editor.recordAndApplyUndoable(item2)
        editor.recordAndApplyUndoable(item3)

        undoSystem.maxHistorySize = 10

        Assertions.assertEquals(0, undoSystem.getEditHistorySize())
    }

    @Test
    fun `canUndo() returns false when history is empty`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val editor = model.editor
        val undoSystem = editor.undoSystem
        undoSystem.maxHistorySize = 3

        Assertions.assertEquals(0, undoSystem.getEditHistorySize())
        Assertions.assertFalse(undoSystem.canUndo())
    }

    @Test
    fun `canRedo() returns false when history is empty`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val editor = model.editor
        val undoSystem = editor.undoSystem
        undoSystem.maxHistorySize = 3

        Assertions.assertEquals(0, undoSystem.getEditHistorySize())
        Assertions.assertFalse(undoSystem.canRedo())
    }


    @Test
    fun `State is correct when performing undo consecutively`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val editor = model.editor
        val undoSystem = editor.undoSystem
        undoSystem.maxHistorySize = 4

        Assertions.assertEquals(0, undoSystem.getEditHistorySize())

        val item1 = TestEdit()
        val item2 = TestEdit()
        val item3 = TestEdit()
        val item4 = TestEdit()

        editor.recordAndApplyUndoable(item1)
        editor.recordAndApplyUndoable(item2)
        editor.recordAndApplyUndoable(item3)
        editor.recordAndApplyUndoable(item4)
        val originalHistorySize = undoSystem.getEditHistorySize()

        // Method under test
        undoSystem.performUndo()
        runStateAssertions(originalHistorySize, 2,
            canRedo = true,
            canUndo = true,
            redoRatherThanUndo = false,
            undoSystem = undoSystem
        )
        undoSystem.performUndo()
        runStateAssertions(originalHistorySize, 1,
            canRedo = true,
            canUndo = true,
            redoRatherThanUndo = false,
            undoSystem = undoSystem
        )
        undoSystem.performUndo()
        runStateAssertions(originalHistorySize, 0,
            canRedo = true,
            canUndo = true,
            redoRatherThanUndo = false,
            undoSystem = undoSystem
        )
        undoSystem.performUndo()
        runStateAssertions(originalHistorySize, -1,
            canRedo = true,
            canUndo = false,
            redoRatherThanUndo = false,
            undoSystem = undoSystem
        )
    }

    @Test
    fun `Undo consecutively then perform new edit, discards edits beyond current position`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val editor = model.editor
        val undoSystem = editor.undoSystem
        undoSystem.maxHistorySize = 4

        Assertions.assertEquals(0, undoSystem.getEditHistorySize())
        val item1 = TestEdit()
        val item2 = TestEdit()
        val item3 = TestEdit()
        val item4 = TestEdit()

        editor.recordAndApplyUndoable(item1)
        editor.recordAndApplyUndoable(item2)
        editor.recordAndApplyUndoable(item3)
        editor.recordAndApplyUndoable(item4)

        val originalHistorySize = 4
        undoSystem.performUndo()
        runStateAssertions(originalHistorySize, 2,
            canRedo = true,
            canUndo = true,
            redoRatherThanUndo = false,
            undoSystem = undoSystem
        )
        undoSystem.performUndo()
        runStateAssertions(originalHistorySize, 1,
            canRedo = true,
            canUndo = true,
            redoRatherThanUndo = false,
            undoSystem = undoSystem
        )

        // Method under test
        editor.recordAndApplyUndoable(TestEdit())
        runStateAssertions(3, 2,
            canRedo = false,
            canUndo = true,
            redoRatherThanUndo = false,
            undoSystem = undoSystem
        )
    }

    @Test
    fun `Undo consecutively then perform identical edit at mid-position, discards newer edits anyway`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val editor = model.editor
        val undoSystem = editor.undoSystem
        undoSystem.maxHistorySize = 4

        Assertions.assertEquals(0, undoSystem.getEditHistorySize())
        val item1 = TestEdit()
        val item2 = TestEdit()
        val item3 = TestEdit()
        val item4 = TestEdit()

        editor.recordAndApplyUndoable(item1)
        editor.recordAndApplyUndoable(item2)
        editor.recordAndApplyUndoable(item3)
        editor.recordAndApplyUndoable(item4)

        val originalHistorySize = 4
        undoSystem.performUndo()
        runStateAssertions(originalHistorySize, 2,
            canRedo = true,
            canUndo = true,
            redoRatherThanUndo = false,
            undoSystem = undoSystem
        )
        undoSystem.performUndo()
        runStateAssertions(originalHistorySize, 1,
            canRedo = true,
            canUndo = true,
            redoRatherThanUndo = false,
            undoSystem = undoSystem
        )

        // Method under test
        editor.recordAndApplyUndoable(item3)
        runStateAssertions(3, 2,
            canRedo = false,
            canUndo = true,
            redoRatherThanUndo = false,
            undoSystem = undoSystem
        )
    }

    @Test
    fun testHasHistory_UndoConsecutivelyThenPerformRedo_StateCorrect() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val editor = model.editor
        val undoSystem = editor.undoSystem
        undoSystem.maxHistorySize = 4

        Assertions.assertEquals(0, undoSystem.getEditHistorySize())
        val item1 = TestEdit()
        val item2 = TestEdit()
        val item3 = TestEdit()
        val item4 = TestEdit()

        editor.recordAndApplyUndoable(item1)
        editor.recordAndApplyUndoable(item2)
        editor.recordAndApplyUndoable(item3)
        editor.recordAndApplyUndoable(item4)

        val originalHistorySize = 4

        undoSystem.performUndo()
        runStateAssertions(originalHistorySize, 2,
            canRedo = true,
            canUndo = true,
            redoRatherThanUndo = false,
            undoSystem = undoSystem
        )
        undoSystem.performUndo()
        runStateAssertions(originalHistorySize, 1,
            canRedo = true,
            canUndo = true,
            redoRatherThanUndo = false,
            undoSystem = undoSystem
        )

        // Method under test
        undoSystem.performRedo()
        runStateAssertions(originalHistorySize, 2,
            canRedo = true,
            canUndo = true,
            redoRatherThanUndo = true,
            undoSystem = undoSystem
        )
        undoSystem.performRedo()
        runStateAssertions(originalHistorySize, 3,
            canRedo = false,
            canUndo = true,
            redoRatherThanUndo = true,
            undoSystem = undoSystem
        )
    }

    private fun runStateAssertions(expectedHistorySize: Int,
                                   expectedPositionInHistory: Int,
                                   canRedo: Boolean,
                                   canUndo: Boolean,
                                   redoRatherThanUndo: Boolean,
                                   undoSystem: GameModelUndoSystem) {

        Assertions.assertEquals(expectedHistorySize, undoSystem.getEditHistorySize(),
            if (redoRatherThanUndo) "Redo" else "Undo" + " caused history size to change.")
        Assertions.assertEquals(expectedPositionInHistory, undoSystem.positionInHistory,
            "Unexpected history in position.")
        Assertions.assertEquals(canRedo, undoSystem.canRedo(), "Unexpected redo-ability state.")
        Assertions.assertEquals(canUndo, undoSystem.canUndo(), "Unexpected undo-ability state.")
    }
}