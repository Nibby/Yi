package codes.nibby.yi.models.editor.edit

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import codes.nibby.yi.models.GameModel
import codes.nibby.yi.models.StandardGameRules
import codes.nibby.yi.models.editor.edit.RemoveNodeEdit
import codes.nibby.yi.models.Stone
import codes.nibby.yi.models.StoneColor

class RemoveNodeEditTest {

    @Test
    fun `Remove one node works`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        val submitResult = model.editor.addMove(1, 1)
        val submittedNode = submitResult.moveNode!!
        Assertions.assertEquals(submittedNode, model.currentNode)

        undoableEditor.recordAndApplyUndoable(RemoveNodeEdit(submittedNode))

        Assertions.assertEquals(model.getRootNode(), model.currentNode)
        Assertions.assertTrue(model.getRootNode().isLastMoveInThisVariation())
    }

    @Test
    fun `Remove subtree works`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        model.beginMoveSequence().playMove(2, 2).playMove(1, 1).playMove(0, 1)
        model.toPreviousNode()
        model.beginMoveSequence().playMove(1, 2)
        val subtreeToRemove = model.getRootNode().getChildNodeInMainBranch()!!

        undoableEditor.recordAndApplyUndoable(RemoveNodeEdit(subtreeToRemove))

        Assertions.assertEquals(model.getRootNode(), model.currentNode)
        Assertions.assertTrue(model.getRootNode().isLastMoveInThisVariation())
    }

    @Test
    fun `Remove one node undo works`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        val submitResult = model.editor.addMove(1, 1)
        val submittedNode = submitResult.moveNode!!
        Assertions.assertEquals(submittedNode, model.currentNode)

        undoableEditor.recordAndApplyUndoable(RemoveNodeEdit(submittedNode))
        Assertions.assertTrue(undoSystem.canUndo())
        undoSystem.performUndo()

        Assertions.assertEquals(submittedNode, model.currentNode)
    }

    @Test
    fun `Remove one node redo works`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        val submitResult = model.editor.addMove(1, 1)
        val submittedNode = submitResult.moveNode!!
        Assertions.assertEquals(submittedNode, model.currentNode)

        undoableEditor.recordAndApplyUndoable(RemoveNodeEdit(submittedNode))
        Assertions.assertTrue(undoSystem.canUndo())
        undoSystem.performUndo()
        Assertions.assertTrue(undoSystem.canRedo())
        undoSystem.performRedo()

        Assertions.assertEquals(model.getRootNode(), model.currentNode)
        Assertions.assertTrue(model.getRootNode().isLastMoveInThisVariation())
    }

    @Test
    fun `Remove subtree undo works`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 5

        model.beginMoveSequence().playMove(2, 2).playMove(1, 1).playMove(0, 1)
        model.toPreviousNode()
        model.beginMoveSequence().playMove(1, 2)
        val subtreeToRemove = model.getRootNode().getChildNodeInMainBranch()!!

        undoableEditor.recordAndApplyUndoable(RemoveNodeEdit(subtreeToRemove))
        Assertions.assertTrue(undoSystem.canUndo())
        undoSystem.performUndo()

        Assertions.assertFalse(model.getRootNode().isLastMoveInThisVariation())

        var currentNode = model.getRootNode().getChildNodeInMainBranch()!!
        Assertions.assertEquals(Stone(2, 2, StoneColor.BLACK), currentNode.getPrimaryMove())
        currentNode = currentNode.getChildNodeInMainBranch()!!
        Assertions.assertEquals(Stone(1, 1, StoneColor.WHITE), currentNode.getPrimaryMove())
        val mainBranchChild = currentNode.getChildNodeInMainBranch()!!
        Assertions.assertEquals(Stone(0, 1, StoneColor.BLACK), mainBranchChild.getPrimaryMove())
        val variationChild = currentNode.getChildNodesExcludingMainBranch()[0]
        Assertions.assertEquals(Stone(1, 2, StoneColor.BLACK), variationChild.getPrimaryMove())
    }

    @Test
    fun `Remove subtree redo works`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        model.beginMoveSequence().playMove(2, 2).playMove(1, 1).playMove(0, 1)
        model.toPreviousNode()
        model.beginMoveSequence().playMove(1, 2)
        val subtreeToRemove = model.getRootNode().getChildNodeInMainBranch()!!

        undoableEditor.recordAndApplyUndoable(RemoveNodeEdit(subtreeToRemove))
        Assertions.assertTrue(undoSystem.canUndo())
        undoSystem.performUndo()
        Assertions.assertTrue(undoSystem.canRedo())
        undoSystem.performRedo()

        Assertions.assertEquals(model.getRootNode(), model.currentNode)
        Assertions.assertTrue(model.getRootNode().isLastMoveInThisVariation())
    }
}