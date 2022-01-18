package codes.nibby.yi.models

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import codes.nibby.yi.models.TestGameRules.TestingGameRulesNoSuicide
import codes.nibby.yi.models.TestGameRules.TestingGameRulesSuicideAllowed

class PlayMoveTest {

    @Test
    fun `play first legal move has correct move tree state`() {
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())

        val result = model.editor.addMove(0, 0)

        Assertions.assertEquals(result.moveNode!!, model.currentNode)
    }

    @Test
    fun `play first legal move returns correct move submission result`() {
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())

        val moveSubmitResult = model.editor.addMove(0, 0)

        Assertions.assertEquals(MoveValidationResult.OK, moveSubmitResult.validationResult)
        Assertions.assertNotNull(moveSubmitResult.moveNode)
        Assertions.assertTrue(moveSubmitResult.isPlayed)
    }

    @Test
    fun `play first legal move resolves to correct board position`() {
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())

        val moveSubmitResult = model.editor.addMove(0, 0)
        val newNode = moveSubmitResult.moveNode

        val firstMoveGameState = model.getGameState(newNode!!)
        val stoneColorAtPlayedMove = firstMoveGameState.boardPosition.getStoneColorAt(0, 0)

        Assertions.assertEquals(StoneColor.BLACK, stoneColorAtPlayedMove)
    }

    @Test
    fun `play five consecutive moves all have correct stone color`() {
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())

        val moveSubmitResult = model.editor.addMove(0, 0)
        val newNode = moveSubmitResult.moveNode

        val firstMoveGameState = model.getGameState(newNode!!)
        val stoneColorAtPlayedMove = firstMoveGameState.boardPosition.getStoneColorAt(0, 0)

        Assertions.assertEquals(StoneColor.BLACK, stoneColorAtPlayedMove)
    }

    @Test
    fun `one stone capture works`() {
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())

        // Play black stone in the corner
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)  // white removes a liberty
                .pass()
                .playMove(0, 1) // white removes last liberty, stone at 0,0 should be captured now

        val gameState = model.getGameState(model.currentNode)
        Assertions.assertEquals(1, gameState.prisonersWhite)
        Assertions.assertEquals(StoneColor.NONE, gameState.boardPosition.getStoneColorAt(0, 0))
    }

    @Test
    fun `connected string capture works with primary moves`() {
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())

        // Black plays three stones along the 1st column, white surrounds it on the 2nd column
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)
                .playMove(0, 1)
                .playMove(1, 1)
                .playMove(0, 2)
                .playMove(1, 2) // After white plays this move, the three black stones on the left column should be all captured

        val currentState = model.getGameState(model.currentNode)

        Assertions.assertEquals(3, currentState.prisonersWhite)

        // Check that stones are actually captured
        Assertions.assertEquals(StoneColor.NONE, currentState.boardPosition.getStoneColorAt(0, 0))
        Assertions.assertEquals(StoneColor.NONE, currentState.boardPosition.getStoneColorAt(0, 1))
        Assertions.assertEquals(StoneColor.NONE, currentState.boardPosition.getStoneColorAt(0, 2))
        Assertions.assertEquals(StoneColor.WHITE, currentState.boardPosition.getStoneColorAt(1, 0))
        Assertions.assertEquals(StoneColor.WHITE, currentState.boardPosition.getStoneColorAt(1, 1))
        Assertions.assertEquals(StoneColor.WHITE, currentState.boardPosition.getStoneColorAt(1, 2))
    }

    @Test
    fun `game model state hash is correct after playing two legal moves`() {
        val model = GameModel(2, 2, TestingGameRulesNoSuicide(), TestingFourIntersectionXORHasher())

        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)

        val stateHashHistory = model.getStateHashHistory()

        Assertions.assertEquals(2, stateHashHistory.size)
        Assertions.assertEquals(13, model.currentNode.getStateHash()) // 0100b xor 1001b = 1101b (13)
        Assertions.assertEquals(4, model.currentNode.parent!!.getStateHash()) // 0100b (4)
    }

    @Test
    fun `game model state hash ignores pass moves`() {
        val model = GameModel(2, 2, TestingGameRulesNoSuicide(), TestingFourIntersectionXORHasher())

        model.beginMoveSequence()
                .playMove(0, 0)
                .pass()

        val stateHashHistory = model.getStateHashHistory()

        Assertions.assertEquals(1, stateHashHistory.size)
        Assertions.assertEquals(4, model.currentNode.parent!!.getStateHash()) // 0100b (4)
    }

    @Test
    fun `played move with no liberty but captures opponent stones is allowed`() {
        val model = GameModel(2, 2, TestingGameRulesNoSuicide(), TestingFourIntersectionXORHasher())

        model.beginMoveSequence()
                .playMove(0, 0)
                .pass()
                .playMove(1, 0)
                .pass()
                .playMove(0, 1)

        val result = model.editor.addMove(1, 1) // This white move is played at an intersection with no liberties, but it captures black first so it is allowed

        Assertions.assertEquals(MoveValidationResult.OK, result.validationResult)
    }

    @Test
    fun `ko recapture is illegal`() {
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())

        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)
                .playMove(1, 1)
                .playMove(0, 1) // white captures black at 0,0
                .playMove(2, 0) // black sets up atari on 1,0 stone
                .pass() // white passes so black can capture 1,0 by playing at 0,0
                .playMove(0, 0) // black captures and starts ko

        // This move should now be illegal because 1,0 was just captured
        val submitResult = model.editor.addMove(1, 0)

        Assertions.assertEquals(MoveValidationResult.ERROR_KO_RECAPTURE, submitResult.validationResult)
    }

    @Test
    fun `suicidal move not allowed on rules that prohibit suicide`() {
        val model = GameModel(2, 2, TestingGameRulesNoSuicide())

        model.beginMoveSequence()
                .playMove(1, 0)
                .pass()
                .playMove(0, 1)

        val submitResult = model.editor.addMove(0, 0) // Tries to play inside black territory (0 liberties)

        Assertions.assertEquals(MoveValidationResult.ERROR_MOVE_SUICIDAL, submitResult.validationResult)
    }

    @Test
    fun `suicidal move is playable on rules that allow suicide`() {
        val model = GameModel(2, 2, TestingGameRulesSuicideAllowed(), TestingFourIntersectionXORHasher())

        model.beginMoveSequence()
                .playMove(1, 0)
                .pass()
                .playMove(0, 1)
                .pass()
                .playMove(1, 1)
                .pass()

        val submitResult = model.editor.addMove(0, 0) // Tries to play inside black territory (0 liberties

        Assertions.assertEquals(MoveValidationResult.OK, submitResult.validationResult)
    }

    @Test
    fun `board position repeat is illegal on 1x1`() {
        val model = GameModel(1, 1, TestingGameRulesSuicideAllowed(), TestingFourIntersectionXORHasher())

        model.beginMoveSequence()
                .playMove(0, 0)

        val result = model.editor.addMove(0, 0) // White tries to play the suicidal move again, which is a suicide and not permitted

        Assertions.assertEquals(MoveValidationResult.ERROR_POSITION_REPEAT, result.validationResult)
    }

    @Test
    fun `board position repeat is illegal on 2x2`() {
        val model = GameModel(2, 2, TestingGameRulesSuicideAllowed(), TestingFourIntersectionXORHasher())

        model.beginMoveSequence()
                .playMove(0, 0)
                .pass()
                .playMove(1, 0)
                .pass()
                .playMove(0, 1)
                .pass()
                .playMove(1, 1) // After playing this move black's entire group is self-captured. This move is not a board state repeat (of root empty state) because that doesn't count as a valid unique state.
                .pass()

        val result = model.editor.addMove(0, 0) // Playing at the position of move 1 again, which is a board position repeat

        Assertions.assertEquals(MoveValidationResult.ERROR_POSITION_REPEAT, result.validationResult)
    }

    @Test // Bug picked up from issue #36
    fun `child nodes enumerated properly as moves are played`() {
        val model = GameModel(2, 2, TestingGameRulesSuicideAllowed(), TestingFourIntersectionXORHasher())

        model.beginMoveSequence()
                .playMove(0, 0)

        val currentMove = model.currentNode
        Assertions.assertEquals(1, model.getCurrentMoveNumber()) // Sanity check

        val parent = currentMove.parent
        Assertions.assertEquals(1, parent!!.children.size)
    }

    @Test
    fun `play move at same coordinate as a previous played move will go to that move instead`() {
        val model = GameModel(2, 2, TestingGameRulesSuicideAllowed(), TestingFourIntersectionXORHasher())

        model.beginMoveSequence()
                .playMove(0, 0)

        // Assume both playMoves() succeed
        val variationOne = model.editor.addMove(1, 0).moveNode
        model.toPreviousNode()
        val variationTwo = model.editor.addMove(0, 1).moveNode

        // back to first node, now playing (1, 0) should set current position to variationOne
        // and playing (0, 1) should set current position to variationTwo without creating new
        // nodes for both situations.
        model.toPreviousNode()

        Assertions.assertEquals(variationOne, model.editor.addMove(1, 0).moveNode)
        model.toPreviousNode()

        Assertions.assertEquals(variationTwo, model.editor.addMove(0, 1).moveNode)
        model.toPreviousNode()

        // Check child size is still correct
        Assertions.assertEquals(2, model.currentNode.children.size)
    }

    @Test
    fun `pass at a position where the next move is also pass will set current move to that node instead`() {
        val model = GameModel(2, 2, TestingGameRulesSuicideAllowed(), TestingFourIntersectionXORHasher())

        val branchNode = model.editor.addMove(0, 0).moveNode!!

        model.editor.addMove(1, 0)
        model.toPreviousNode()
        val firstPassNode = model.editor.addPass().moveNode!!
        model.toPreviousNode()

        // Check setup is correct
        Assertions.assertEquals(2, model.currentNode.children.size)

        // Pass again. This should set the current move to firstPassNode.
        // No new branch will be created.
        model.editor.addPass()

        Assertions.assertEquals(2, branchNode.children.size, "Child size changed after passing the second time.")
        Assertions.assertEquals(firstPassNode, model.currentNode, "Current node is not at the original pass node.")
    }

    @Test
    fun `play move, remove it, play there again at root still works`() {
        val model = GameModel(2, 2, TestingGameRulesSuicideAllowed(), TestingFourIntersectionXORHasher())
        model.beginMoveSequence().playMove(0, 0)
        model.editor.removeNodeSubtree(model.getRootNode())

        // Method under test
        val result = model.editor.addMove(0, 0)

        // Assert
        Assertions.assertEquals(MoveValidationResult.OK, result.validationResult)
    }

    // Only supports a 2x2 board for testing purposes
    private class TestingFourIntersectionXORHasher : GameStateHasher {

        // Uses the first four bits of a 'byte' to represent unique hash values for each intersection state
        // bit format: SS PP, where S = stone color, P = intersection position
        //
        // state table: 00 = empty intersection
        //              01 = black stone
        //              10 = white stone
        //              11 = [ unused ]
        //
        // position: 00 = (0,0)
        //           01 = (0,1)
        //           10 = (1,0)
        //           11 = (1,1)

        val hashes = arrayOf(
                0, 1, 2, 3,
                4, 5, 6, 7,
                8, 9, 10, 11
        )

        override fun computeStateHash(state: GameState, boardWidth: Int, boardHeight: Int): Long {
            val position = state.boardPosition
            var stateHash = computeEmptyPositionHash(boardWidth, boardHeight)

            for (intersection in 0..3) {
                val stoneColor = position.getStoneColorAt(intersection)
                val stateId = stoneColor.index
                val hash = hashes[stateId * 4 + intersection].toLong()

                stateHash = stateHash xor hash
            }
            return stateHash
        }

        override fun computeUpdateHash(lastStateHash: Long, stoneUpdates: Set<Stone>): Long {
            var newHash = lastStateHash
            stoneUpdates.forEach { update ->
                newHash = newHash xor getHash(update.color.index, update.x + update.y * 2)
            }

            return newHash
        }

        fun getHash(stateId: Byte, position: Int): Long {
            return hashes[stateId * 4 + position].toLong()
        }
    }
}