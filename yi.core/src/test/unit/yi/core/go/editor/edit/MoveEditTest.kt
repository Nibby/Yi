package yi.core.go.editor.edit

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.*
import yi.core.go.EventListener
import java.util.*

class MoveEditTest {

    @Test
    fun `Play move creates undo history item`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        // Method under test
        val item1 = MoveEdit.playedMove(0, 0)
        undoableEditor.recordAndApplyUndoable(item1)

        Assertions.assertEquals(1, undoSystem.getEditHistorySize())
        Assertions.assertEquals(item1, undoSystem.getHistoryItem(0),
            "Incorrect undo stack state. item2 is not the bottom-most item.")
    }

    @Test
    fun `Perform undo on one played move restores state correctly`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        // Method under test
        val item1 = MoveEdit.playedMove(0, 0)
        undoableEditor.recordAndApplyUndoable(item1)

        // Method under test
        Assertions.assertTrue(undoSystem.canUndo(), "Cannot undo after playing one move.")
        undoSystem.performUndo()

        Assertions.assertEquals(model.getRootNode(), model.currentNode,
            "Current move not adjusted to root node after undoing first move.")
    }

    @Test
    fun `Undo played move consecutively restores model state correctly`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 9

        undoableEditor.recordAndApplyUndoable(MoveEdit.playedMove(0, 0))
        undoableEditor.recordAndApplyUndoable(MoveEdit.playedMove(1, 0))
        undoableEditor.recordAndApplyUndoable(MoveEdit.playedMove(2, 0))
        undoableEditor.recordAndApplyUndoable(MoveEdit.playedMove(0, 1))
        undoableEditor.recordAndApplyUndoable(MoveEdit.playedMove(1, 1))
        undoableEditor.recordAndApplyUndoable(MoveEdit.playedMove(2, 1))
        undoableEditor.recordAndApplyUndoable(MoveEdit.playedMove(0, 2))
        undoableEditor.recordAndApplyUndoable(MoveEdit.playedMove(1, 2))

        val initialBoardPosition = arrayOf(
            " o ",
            "o o",
            " o "
        )

        // Test setup is correct
        Assertions.assertArrayEquals(initialBoardPosition, getBoardPosition(model), "Initial board position mismatch")
        Assertions.assertTrue(undoSystem.canUndo(), "Cannot begin undo")

        val expectedPositions = Array<Array<String>>(8) { arrayOf() }

        expectedPositions[0] = arrayOf(
            " o ",
            "oxo",
            "x  ",
        )
        expectedPositions[1] = arrayOf(
            " o ",
            "oxo",
            "   ",
        )
        expectedPositions[2] = arrayOf(
            " ox",
            "ox ",
            "   "
        )
        expectedPositions[3] = arrayOf(
            " ox",
            "o  ",
            "   "
        )
        expectedPositions[4] = arrayOf(
            "xox",
            "   ",
            "   "
        )
        expectedPositions[5] = arrayOf(
            "xo ",
            "   ",
            "   "
        )
        expectedPositions[6] = arrayOf(
            "x  ",
            "   ",
            "   "
        )
        expectedPositions[7] = arrayOf(
            "   ",
            "   ",
            "   "
        )
        

        // Begin test
        for (step in expectedPositions.indices) {
            undoSystem.performUndo()

            Assertions.assertArrayEquals(expectedPositions[step], getBoardPosition(model),
                "Board position mismatch after undo once.")
            Assertions.assertEquals(8 - step, model.currentNode.getMoveHistory().size,
                "Model node length differs from undo steps taken. " +
                    "Error occurred at step " + step + ". The tree structure is not in sync with undo stack.")

            if (step <= initialBoardPosition.size - 1) {
                Assertions.assertTrue(undoSystem.canUndo(),
                    "Cannot undo but not at the bottom of undo stack! Current position: "
                            + undoSystem.positionInHistory + ", "
                            + "stack size: " + undoSystem.getEditHistorySize()
                            + ". Steps: " + step)
            }
        }

        Assertions.assertFalse(undoSystem.canUndo(),
            "Undo still enabled but we are already at the bottom of undo stack! " +
                    "Current position: " + undoSystem.positionInHistory + ", " +
                    "stack size: " + undoSystem.getEditHistorySize())

        Assertions.assertEquals(0, model.getCurrentMoveNumber(),
            "Not at root node after performing all undo steps.")
    }

    @Test
    fun `Undo correctly fires current node change event`() {
        var eventReceived = false
        var newNodeFromEvent: GameNode? = null

        val eventListener = object : EventListener<NodeEvent> {
            override fun onEvent(event: NodeEvent) {
                eventReceived = true
                newNodeFromEvent = event.node
            }
        }

        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        model.onCurrentNodeChange().addListener(eventListener)

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        // Method under test
        val item1 = MoveEdit.playedMove(0, 0)
        undoableEditor.recordAndApplyUndoable(item1)

        // Method under test
        Assertions.assertTrue(undoSystem.canUndo(), "Cannot undo after playing one move.")
        undoSystem.performUndo()

        Thread.sleep(10)

        Assertions.assertTrue(eventReceived,
            "Current node change event not received after performing undo")
        Assertions.assertEquals(model.getRootNode(), newNodeFromEvent,
            "Current node change event did not pass the correct new node after undo")
    }

    @Test
    fun `Redo adjusts current move`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        // Method under test
        val item1 = MoveEdit.playedMove(0, 0)
        undoableEditor.recordAndApplyUndoable(item1)

        Assertions.assertEquals(model.getRootNode().getChildNodeInMainBranch(), model.currentNode,
            "Current move not at root after undo from one move tree.")

        undoSystem.performUndo()
        Assertions.assertEquals(model.getRootNode(), model.currentNode,
            "Current move not at root after undo from one move tree.")

        // Method under test
        undoSystem.performRedo()
        Assertions.assertEquals(model.getRootNode().getChildNodeInMainBranch(), model.currentNode,
            "Current move not adjusted to root node after performing redo.")
    }

    @Test
    fun `Undo then redo with many branches reconstructs model state correctly`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        val branchMain = MoveEdit.playedMove(2, 0)

        undoableEditor.recordAndApplyUndoable(MoveEdit.playedMove(0, 0))
        undoableEditor.recordAndApplyUndoable(MoveEdit.playedMove(1, 0)) // <- Create branches from here
        undoableEditor.recordAndApplyUndoable(branchMain)

        // Make some branches of size 1 at the 2nd move
        val branch1 = MoveEdit.playedMove(0, 2)
        val branch2 = MoveEdit.playedMove(2, 1)
        val branch3 = MoveEdit.playedMove(2, 2)
        val branch4 = MoveEdit.playedMove(1, 1)
        val branch5 = MoveEdit.playedMove(1, 2)
        val branch6 = MoveEdit.playedMove(0, 1)

        model.toPreviousNode()
        undoableEditor.recordAndApplyUndoable(branch1)
        model.toPreviousNode()
        undoableEditor.recordAndApplyUndoable(branch2)
        model.toPreviousNode()
        undoableEditor.recordAndApplyUndoable(branch3)
        model.toPreviousNode()
        undoableEditor.recordAndApplyUndoable(branch4)
        model.toPreviousNode()
        undoableEditor.recordAndApplyUndoable(branch5)
        model.toPreviousNode()
        undoableEditor.recordAndApplyUndoable(branch6)
        model.toPreviousNode() // Restore back to branching point

        val branchesIncludingMain = model.currentNode.getChildNodes()
        Assertions.assertEquals(7, branchesIncludingMain.size)

        // The branches we expect to remain on the tree after each undo
        val branchStack = Stack<GameNode>()
        branchStack.push(branchMain.getSubmittedNode()!!)
        branchStack.push(branch1.getSubmittedNode()!!)
        branchStack.push(branch2.getSubmittedNode()!!)
        branchStack.push(branch3.getSubmittedNode()!!)
        branchStack.push(branch4.getSubmittedNode()!!)
        branchStack.push(branch5.getSubmittedNode()!!)
        branchStack.push(branch6.getSubmittedNode()!!)

        // The branches popped off the undo history due to redo, that we expect to be restored in this order
        // in the subsequent redo.
        val poppedStack = Stack<GameNode>()

        // Undo it
        for (i in 0 until 6) {
            Assertions.assertTrue(undoSystem.canUndo(),
                "Cannot undo anymore after $i undo step(s)")
            undoSystem.performUndo()
            poppedStack.push(branchStack.pop())

            // Check the remaining branches are in the identical order as given by branchStack
            for (childPosition in branchesIncludingMain.indices) {
                val expectedNodeInThisPosition = branchStack[childPosition]
                Assertions.assertEquals(expectedNodeInThisPosition,
                    branchesIncludingMain[childPosition],
                    "Child nodes (branch) order is incorrect after performing $i steps of undo."
                )
            }
        }

        // Redo it
        for (i in 0 until 6) {
            Assertions.assertTrue(undoSystem.canUndo(),
                "Cannot redo anymore after $i undo step(s)")
            undoSystem.performRedo()
            branchStack.push(poppedStack.pop())

            Assertions.assertEquals(2 + i, branchesIncludingMain.size, "Branch size unexpected")
            // Check the remaining branches are in the identical order as given by branchStack
            for (childPosition in branchesIncludingMain.indices) {
            val expectedNodeInThisPosition = branchStack[childPosition]
            Assertions.assertEquals(expectedNodeInThisPosition,
                branchesIncludingMain[childPosition],
                "Child nodes (branch) order is incorrect after performing $i steps of redo."
            )
        }
        }
    }

    @Test
    fun `Play pass works`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        val pass = MoveEdit.pass()
        model.editor.recordAndApplyUndoable(pass)

        Assertions.assertTrue(undoSystem.canUndo())
        Assertions.assertFalse(undoSystem.canRedo())
        Assertions.assertEquals(1, undoSystem.getEditHistorySize())
        Assertions.assertEquals(pass, undoSystem.getHistoryItem(0))

        Assertions.assertEquals(GameNodeType.PASS, model.currentNode.getType())
    }

    @Test
    fun `Play pass undo correctly`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        val pass = MoveEdit.pass()
        model.editor.recordAndApplyUndoable(pass)

        undoSystem.performUndo()

        Assertions.assertFalse(undoSystem.canUndo())
        Assertions.assertTrue(undoSystem.canRedo())
        Assertions.assertEquals(1, undoSystem.getEditHistorySize())
    }

    @Test
    fun `Play pass undo redo, state is correct`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        val pass = MoveEdit.pass()
        model.editor.recordAndApplyUndoable(pass)

        undoSystem.performUndo()
        undoSystem.performRedo()

        Assertions.assertTrue(undoSystem.canUndo())
        Assertions.assertFalse(undoSystem.canRedo())
        Assertions.assertEquals(1, undoSystem.getEditHistorySize())
        Assertions.assertEquals(pass, undoSystem.getHistoryItem(0))

        Assertions.assertEquals(GameNodeType.PASS, model.currentNode.getType())
    }

    @Test
    fun `Play pass, go back to parent, then play pass again, reuses the same node`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        val pass = MoveEdit.pass()
        model.editor.recordAndApplyUndoable(pass)
        model.currentNode = model.getRootNode()

        // Second pass. We would expect this edit to be ignored because the previous
        // pass is already a continuation of root node.
        model.editor.recordAndApplyUndoable(MoveEdit.pass())

        Assertions.assertEquals(1, undoSystem.getEditHistorySize())
        Assertions.assertEquals(pass, undoSystem.getHistoryItem(0))
    }

    @Test
    fun `Play pass, go back to parent, then play pass again, undo is correct`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)

        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        val pass = MoveEdit.pass()
        model.editor.recordAndApplyUndoable(pass)
        model.currentNode = model.getRootNode()

        // Second pass. We would expect this edit to be ignored because the previous
        // pass is already a continuation of root node.
        model.editor.recordAndApplyUndoable(MoveEdit.pass())

        undoSystem.performUndo()

        Assertions.assertEquals(1, undoSystem.getEditHistorySize())
        Assertions.assertEquals(pass, undoSystem.getHistoryItem(0))
        Assertions.assertEquals(model.getRootNode(), model.currentNode)
        Assertions.assertTrue(model.getRootNode().isLastMoveInThisVariation())
    }

    // TODO: Add these once pass and resign edits are properly implemented.
//    @Test
//    fun testUndo_Pass_StateCorrect() {
//        var model = new GameModel(3, 3, GameRules.CHINESE)
//        var manager = GameBoardClassFactory.createGameBoardManager()
//        GameBoardManagerAccessor.setGameModel(manager, model)
//
//        var editor = GameBoardClassFactory.createGameModelEditor()
//        editor.setMaxHistorySize(50)
//
//        editor.recordAndApply(PlayMoveEdit.forPass(), manager)
//        Assertions.assertEquals(1, editor.getCurrentHistorySize())
//    }
//
//    @Test
//    fun testRedo_Pass_StateCorrect() {
//        // TODO: Implement me
//    }
//
//    @Test
//    fun testUndo_Resign_StateCorrect() {
//        // TODO: Implement me
//    }
//
//    @Test
//    fun testRedo_Resign_StateCorrect() {
//        // TODO: Implement me
//    }

    private fun getBoardPosition(model: GameModel): Array<String> {
        val state = model.getCurrentGameState()
        val position = state.boardPosition

        val positionData = Array(model.boardHeight) { "" }

        for (y in positionData.indices) {
            val rowData = StringBuilder()
            for (x in 0 until model.boardWidth) {
                when (position.getStoneColorAt(x, y)) {
                    StoneColor.BLACK -> rowData.append("x")
                    StoneColor.WHITE -> rowData.append("o")
                    else -> rowData.append(" ")
                }
            }
            positionData[y] = rowData.toString()
        }
        return positionData
    }
}