package yi.core.go

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.TestGameRules.TestingGameRulesNoSuicide
import java.lang.IllegalArgumentException

class GameModelTest {

    @Test
    fun `model tree state is initialized correctly`() {
        val model = GameModel(19, 19, TestingGameRulesNoSuicide())

        Assertions.assertEquals(model.gameTree.rootNode, model.getCurrentNode())
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

        model.toPreviousNode()
        model.toPreviousNode()
        val nodeToDelete = model.getCurrentNode()
        val whereCurrentNodeShouldBeAt = model.toPreviousNode()!!

        model.toNextNode(3) // Set current move to child of the node to delete
        model.removeNodeSubtree(nodeToDelete) // Should adjust the current node to the parent of nodeToDelete

        Assertions.assertEquals(whereCurrentNodeShouldBeAt, model.getCurrentNode())
    }

    @Test
    fun `add metadata value with empty key fails`() {
        val model = GameModel(19, 19, TestingGameRulesNoSuicide())
        var thrownException = false

        try {
            model.getRootNode().putMetadata("", "this key shouldn't be added")
        } catch (e: IllegalArgumentException) {
            thrownException = true
        }

        if (!thrownException) {
            Assertions.fail<String>("Setting metadata with empty key is permitted when it " +
                    "should have failed.")
        }
    }

    @Test
    fun `add metadata value with whitespace key fails`() {
        val model = GameModel(19, 19, TestingGameRulesNoSuicide())
        var thrownException = false

        try {
            model.getRootNode().putMetadata("    ", "this key shouldn't be added")
        } catch (e: IllegalArgumentException) {
            thrownException = true
        }

        if (!thrownException) {
            Assertions.fail<String>("Setting metadata with whitespaces as key is permitted " +
                    "when it should have failed.")
        }
    }

    @Test
    fun `add metadata value with non-alphanumeric character fails`() {
        val model = GameModel(19, 19, TestingGameRulesNoSuicide())
        var thrownException = false

        try {
            model.getRootNode().putMetadata("J.", "this key shouldn't be added")
        } catch (e: IllegalArgumentException) {
            thrownException = true
        }

        if (!thrownException) {
            Assertions.fail<String>("Setting metadata with key containing non-alphanumeric " +
                    "characters is permitted when it should have failed.")
        }
    }
}