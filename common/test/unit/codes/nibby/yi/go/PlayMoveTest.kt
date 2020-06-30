package codes.nibby.yi.go

import codes.nibby.yi.go.rules.GoGameRulesHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PlayMoveTest {

    @Test
    fun `play first legal move has correct move tree state`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        val result = model.playMove(0, 0)

        Assertions.assertEquals(result.moveNode!!, model.currentNode)
    }

    @Test
    fun `play first legal move returns correct move submission result`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        val moveSubmitResult = model.playMove(0, 0)

        Assertions.assertEquals(MoveValidationResult.OK, moveSubmitResult.validationResult)
        Assertions.assertNotNull(moveSubmitResult.moveNode)
        Assertions.assertTrue(moveSubmitResult.played)
    }

    @Test
    fun `play first legal move resolves to correct board position`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        val moveSubmitResult = model.playMove(0, 0)
        val newNode = moveSubmitResult.moveNode

        val firstMoveGameState = model.resolveGameState(newNode!!)
        val stoneColorAtPlayedMove = firstMoveGameState.gamePosition.getStoneColorAt(0, 0)

        Assertions.assertEquals(GoStoneColor.BLACK, stoneColorAtPlayedMove)
    }

    @Test
    fun `play five consecutive moves all have correct stone color`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        val moveSubmitResult = model.playMove(0, 0)
        val newNode = moveSubmitResult.moveNode

        val firstMoveGameState = model.resolveGameState(newNode!!)
        val stoneColorAtPlayedMove = firstMoveGameState.gamePosition.getStoneColorAt(0, 0)

        Assertions.assertEquals(GoStoneColor.BLACK, stoneColorAtPlayedMove)
    }

    @Test
    fun `one stone capture works`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        // Play black stone in the corner
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)  // white removes a liberty
                .pass()
                .playMove(0, 1) // white removes last liberty, stone at 0,0 should be captured now

        val gameState = model.resolveGameState(model.currentNode)
        Assertions.assertEquals(1, gameState.prisonersWhite)
        Assertions.assertEquals(GoStoneColor.NONE, gameState.gamePosition.getStoneColorAt(0, 0))
    }

    @Test
    fun `connected string capture works with primary moves`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        // Black plays three stones along the 1st column, white surrounds it on the 2nd column
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)
                .playMove(0, 1)
                .playMove(1, 1)
                .playMove(0, 2)
                .playMove(1, 2) // After white plays this move, the three black stones on the left column should be all captured

        val currentState = model.resolveGameState(model.currentNode)

        Assertions.assertEquals(3, currentState.prisonersWhite)

        // Check that stones are actually captured
        Assertions.assertEquals(GoStoneColor.NONE, currentState.gamePosition.getStoneColorAt(0, 0))
        Assertions.assertEquals(GoStoneColor.NONE, currentState.gamePosition.getStoneColorAt(0, 1))
        Assertions.assertEquals(GoStoneColor.NONE, currentState.gamePosition.getStoneColorAt(0, 2))
        Assertions.assertEquals(GoStoneColor.WHITE, currentState.gamePosition.getStoneColorAt(1, 0))
        Assertions.assertEquals(GoStoneColor.WHITE, currentState.gamePosition.getStoneColorAt(1, 1))
        Assertions.assertEquals(GoStoneColor.WHITE, currentState.gamePosition.getStoneColorAt(1, 2))
    }

    @Test
    fun `game model state hash is correct after playing two legal moves`() {
        val model = GoGameModel(2, 2, TestingGameRulesNoSuicide(), TestingFourIntersectionXORHasher())

        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)

        val stateHashHistory = model.getStateHashHistory()

        Assertions.assertEquals(2, stateHashHistory.size)
        Assertions.assertEquals(13, model.currentNode.data!!.stateHash) // 0100b xor 1001b = 1101b (13)
        Assertions.assertEquals(4, model.currentNode.parent!!.data!!.stateHash) // 0100b (4)
    }

    @Test
    fun `game model state hash ignores pass moves`() {
        val model = GoGameModel(2, 2, TestingGameRulesNoSuicide(), TestingFourIntersectionXORHasher())

        model.beginMoveSequence()
                .playMove(0, 0)
                .pass()

        val stateHashHistory = model.getStateHashHistory()

        Assertions.assertEquals(1, stateHashHistory.size)
        Assertions.assertEquals(4, model.currentNode.parent!!.data!!.stateHash) // 0100b (4)
    }

    @Test
    fun `game model state hash ignores resignation moves`() {
        val model = GoGameModel(2, 2, TestingGameRulesNoSuicide(), TestingFourIntersectionXORHasher())

        model.beginMoveSequence()
                .playMove(0, 0)
                .resign()

        val stateHashHistory = model.getStateHashHistory()

        Assertions.assertEquals(1, stateHashHistory.size)
        Assertions.assertEquals(4, model.currentNode.parent!!.data!!.stateHash) // 0100b (4)
    }

    @Test
    fun `ko recapture is illegal`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)
                .playMove(1, 1)
                .playMove(0, 1) // white captures black at 0,0
                .playMove(2, 0) // black sets up atari on 1,0 stone
                .pass() // white passes so black can capture 1,0 by playing at 0,0
                .playMove(0, 0) // black captures and starts ko

        // This move should now be illegal because 1,0 was just captured
        val submitResult = model.playMove(1, 0)

        Assertions.assertEquals(MoveValidationResult.ERROR_KO_RECAPTURE, submitResult.validationResult)
    }

    @Test
    fun `suicidal move not allowed on rules that prohibit suicide`() {
        val model = GoGameModel(2, 2, TestingGameRulesNoSuicide())

        model.beginMoveSequence()
                .playMove(1, 0)
                .pass()
                .playMove(0, 1)

        val submitResult = model.playMove(0, 0) // Tries to play inside black territory (0 liberties)

        Assertions.assertEquals(MoveValidationResult.ERROR_MOVE_SUICIDAL, submitResult.validationResult)
    }

    @Test
    fun `suicidal move is playable on rules that allow suicide`() {
        val model = GoGameModel(2, 2, TestingGameRulesSuicideAllowed())

        model.beginMoveSequence()
                .playMove(1, 0)
                .pass()
                .playMove(0, 1)

        val submitResult = model.playMove(0, 0) // Tries to play inside black territory (0 liberties)

        Assertions.assertEquals(MoveValidationResult.OK, submitResult.validationResult)
    }

    @Test
    fun `board position repeat is illegal`() {
        // TODO: Confirm the exact mechanism of whole board position repeat.
    }


    // Only supports a 2x2 board for testing purposes
    private class TestingFourIntersectionXORHasher() : StateHasher {

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

        override fun calculateStateHash(state: GoGameState): Long {
            val position = state.gamePosition
            var stateHash = 0L

            for (intersection in 0..3) {
                val stoneColor = position.getStoneColorAt(intersection)
                val stateId = stoneColor.index
                val hash = hashes[stateId * 4 + intersection].toLong()

                stateHash = stateHash xor hash
            }
            return stateHash
        }

        override fun calculateUpdateHash(currentStateHash: Long, stoneUpdates: Set<StoneData>): Long {
            var newHash = currentStateHash
            stoneUpdates.forEach { update ->
                newHash = newHash xor getHash(update.stoneColor.index, update.x + update.y * 2)
            }

            return newHash
        }

        fun getHash(stateId: Byte, position: Int): Long {
            return hashes[stateId * 4 + position].toLong()
        }
    }

    private class TestingGameRulesNoSuicide : GoGameRulesHandler() {
        override fun getKomi(): Float = 6.5F
        override fun allowSuicideMoves(): Boolean = false
    }

    private class TestingGameRulesSuicideAllowed : GoGameRulesHandler() {
        override fun getKomi(): Float = 6.5F
        override fun allowSuicideMoves(): Boolean = true
    }
}