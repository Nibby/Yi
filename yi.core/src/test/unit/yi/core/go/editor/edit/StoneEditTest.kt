package yi.core.go.editor.edit

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.*

class StoneEditTest {

    @Test
    fun `Perform stone edit on non-STONE_EDIT node creates a new edit node`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        Assertions.assertNotSame(GameNodeType.STONE_EDIT, model.currentNode.getType())

        model.editor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.WHITE))

        Assertions.assertNotEquals(model.getRootNode(), model.currentNode)
        Assertions.assertEquals(1, model.getCurrentMoveNumber())
        Assertions.assertSame(GameNodeType.STONE_EDIT, model.currentNode.getType())
    }

    @Test
    fun `Adding stone edit of StoneColor NONE to an intersection where stone exists erases it`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        Assertions.assertNotSame(GameNodeType.STONE_EDIT, model.currentNode.getType())

        val undoableEditor = model.editor

        undoableEditor.recordAndApplyUndoable(MoveEdit(0, 0))
        undoableEditor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.NONE))

        Assertions.assertEquals(2, model.getCurrentMoveNumber())
        Assertions.assertSame(GameNodeType.STONE_EDIT, model.currentNode.getType())

        val state = model.getCurrentGameState()
        val position = state.boardPosition
        val finalColor = position.getStoneColorAt(0, 0)

        Assertions.assertEquals(StoneColor.NONE, finalColor)
    }

    @Test
    fun `Perform multiple stone edits on the same node creates multiple edit nodes`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        Assertions.assertNotSame(GameNodeType.STONE_EDIT, model.currentNode.getType())

        val undoableEditor = model.editor
        undoableEditor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.WHITE))
        model.currentNode = model.getRootNode()
        undoableEditor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.WHITE))
        model.currentNode = model.getRootNode()
        undoableEditor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.WHITE))
        model.currentNode = model.getRootNode()

        Assertions.assertEquals(3, model.getRootNode().getChildNodes().size)

        for (childNode in model.getRootNode().getChildNodes()) {
            Assertions.assertEquals(GameNodeType.STONE_EDIT, childNode.getType())
            Assertions.assertEquals(Stone(0, 0, StoneColor.WHITE), childNode.getStoneEditAt(0, 0))
        }
    }

    @Test
    fun `Perform multiple stone edits on the same node, undo all, state correct at each step`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        Assertions.assertNotSame(GameNodeType.STONE_EDIT, model.currentNode.getType())

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoableEditor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.WHITE))
        model.currentNode = model.getRootNode()
        undoableEditor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.WHITE))
        model.currentNode = model.getRootNode()
        undoableEditor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.WHITE))
        model.currentNode = model.getRootNode()

        Assertions.assertEquals(3, model.getRootNode().getChildNodes().size)
        Assertions.assertTrue(undoSystem.canUndo())

        undoSystem.performUndo()
        Assertions.assertTrue(undoSystem.canUndo())
        Assertions.assertEquals(2, model.getRootNode().getChildNodes().size)

        undoSystem.performUndo()
        Assertions.assertTrue(undoSystem.canUndo())
        Assertions.assertEquals(1, model.getRootNode().getChildNodes().size)

        undoSystem.performUndo()
        Assertions.assertFalse(undoSystem.canUndo())
        Assertions.assertEquals(0, model.getRootNode().getChildNodes().size)
    }

    @Test
    fun `Perform multiple stone edits on the same node, undo all, redo all, restores state correctly`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        Assertions.assertNotSame(GameNodeType.STONE_EDIT, model.currentNode.getType())

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoableEditor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.WHITE))
        model.currentNode = model.getRootNode()
        undoableEditor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.WHITE))
        model.currentNode = model.getRootNode()
        undoableEditor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.WHITE))
        model.currentNode = model.getRootNode()

        Assertions.assertFalse(undoSystem.canRedo())
        undoSystem.performUndo()
        undoSystem.performUndo()
        undoSystem.performUndo()

        Assertions.assertTrue(undoSystem.canRedo())
        undoSystem.performRedo()
        Assertions.assertEquals(1, model.getRootNode().getChildNodes().size)

        Assertions.assertTrue(undoSystem.canRedo())
        undoSystem.performRedo()
        Assertions.assertEquals(2, model.getRootNode().getChildNodes().size)

        Assertions.assertTrue(undoSystem.canRedo())
        undoSystem.performRedo()
        Assertions.assertEquals(3, model.getRootNode().getChildNodes().size)
    }

    @Test
    fun `Undo the edit that created a new edit node also removes that node`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val editor = model.editor
        val undoSystem = editor.undoSystem
        undoSystem.maxHistorySize = 3

        Assertions.assertNotSame(GameNodeType.STONE_EDIT, model.currentNode.getType())

        editor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.BLACK))
        undoSystem.performUndo()
        Assertions.assertTrue(model.getRootNode().isLastMoveInThisVariation())
    }

    @Test
    fun `Redo the edit that created a new edit node restores the node`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val editor = model.editor
        val undoSystem = editor.undoSystem
        undoSystem.maxHistorySize = 3

        Assertions.assertNotSame(GameNodeType.STONE_EDIT, model.currentNode.getType())

        editor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.BLACK))
        val firstNode = model.currentNode

        undoSystem.performUndo()
        undoSystem.performRedo()
        Assertions.assertFalse(model.getRootNode().isLastMoveInThisVariation())

        Assertions.assertSame(firstNode, model.getRootNode().getChildNodeInMainBranch()!!)
    }

    @Test
    fun `Adding multiple edits creates only one node`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        Assertions.assertNotSame(GameNodeType.STONE_EDIT, model.currentNode.getType())

        model.editor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.WHITE))

        // These two edits edits the current node because that's the node created. These edits should not create more nodes.
        model.editor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 1, 0, StoneColor.WHITE))
        model.editor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 2, 0, StoneColor.WHITE))

        Assertions.assertNotEquals(model.getRootNode(), model.currentNode)
        Assertions.assertEquals(1, model.getCurrentMoveNumber())
        Assertions.assertSame(GameNodeType.STONE_EDIT, model.currentNode.getType())
        Assertions.assertEquals(3, model.currentNode.getStoneEdits().size)
    }

    @Test
    fun `Stone edit undo redo works for stone additions`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val editor = model.editor
        val undoSystem = editor.undoSystem

        undoSystem.maxHistorySize = 10

        // Setup
        editor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.WHITE))
        editor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 1, StoneColor.WHITE))
        editor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 2, StoneColor.WHITE))

        // Begin undo
        undoSystem.performUndo()
        var position = model.getCurrentGameState().boardPosition
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 2))
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 1))
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 0))

        undoSystem.performUndo()
        position = model.getCurrentGameState().boardPosition
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 2))
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 1))
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 0))

        undoSystem.performUndo()
        position = model.getCurrentGameState().boardPosition
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 2))
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 1))
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 0))

        // Begin redo
        undoSystem.performRedo()
        position = model.getCurrentGameState().boardPosition
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 2))
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 1))
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 0))

        undoSystem.performRedo()
        position = model.getCurrentGameState().boardPosition
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 2))
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 1))
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 0))

        undoSystem.performRedo()
        position = model.getCurrentGameState().boardPosition
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 0))
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 1))
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 0))
    }

    @Test
    fun testSubmitEditNode_UndoRedoWorks() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        // Setup
        val undoableEditor = model.editor

        undoableEditor.recordAndApplyUndoable(MoveEdit(0, 0))
        undoableEditor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 0, StoneColor.NONE))
        undoableEditor.recordAndApplyUndoable(StoneEdit.Add(model.currentNode, 0, 1, StoneColor.WHITE))

        var position = model.getCurrentGameState().boardPosition
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 0))
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 1))
        Assertions.assertNotEquals(model.getRootNode(), model.currentNode)
        Assertions.assertEquals(2, model.getCurrentMoveNumber())

        // Begin undo
        val undoSystem = model.editor.undoSystem
        undoSystem.performUndo()
        position = model.getCurrentGameState().boardPosition
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 0))
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 1))
        Assertions.assertNotEquals(model.getRootNode(), model.currentNode)
        Assertions.assertEquals(2, model.getCurrentMoveNumber())

        // This undo should also remove the stone edit node, restoring current node back to move 1 -- the played black stone
        undoSystem.performUndo()
        position = model.getCurrentGameState().boardPosition
        Assertions.assertEquals(StoneColor.BLACK, position.getStoneColorAt(0, 0))
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 1))
        Assertions.assertEquals(
            model.getRootNode().getChildNodeInMainBranch(),
            model.currentNode
        )
        Assertions.assertEquals(1, model.getCurrentMoveNumber())

        // Begin redo
        undoSystem.performRedo()
        position = model.getCurrentGameState().boardPosition
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 0))
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 1))
        Assertions.assertNotEquals(model.getRootNode(), model.currentNode)
        Assertions.assertEquals(2, model.getCurrentMoveNumber())
        undoSystem.performRedo()
        position = model.getCurrentGameState().boardPosition
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 0))
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 1))
        Assertions.assertNotEquals(model.getRootNode(), model.currentNode)
        Assertions.assertEquals(2, model.getCurrentMoveNumber())
    }
}