package yi.core.go

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.TestGameRules.TestingGameRulesNoSuicide

class GameModelTest {

    @Test
    fun `model tree state is initialized correctly`() {
        val model = GameModel(19, 19, TestingGameRulesNoSuicide())

        Assertions.assertEquals(model.gameTree.rootNode, model.getCurrentMove())
    }

    @Test
    fun `remove parent node subtree adjusts current move to new leaf of the branch`() {
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())

        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0) // <- Expect current move to be set here after deletion
                .playMove(2, 0) // <- Will be deleting this move and its subtree
                .playMove(0, 1)
                .playMove(1, 1) // <- Where current move will be at prior to deletion

        model.toPreviousMove()
        model.toPreviousMove()
        val nodeToDelete = model.getCurrentMove()
        val whereCurrentNodeShouldBeAt = model.toPreviousMove()!!

        model.toNextMove(3) // Set current move to child of the node to delete
        model.removeNodeSubtree(nodeToDelete) // Should adjust the current node to the parent of nodeToDelete

        Assertions.assertEquals(whereCurrentNodeShouldBeAt, model.getCurrentMove())
    }

}