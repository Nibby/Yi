package codes.nibby.yi.go

import codes.nibby.yi.go.rules.GoGameRulesHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration

class PlayMoveTest {

    private class TestingGameRulesNoSuicide : GoGameRulesHandler() {
        override fun getKomi(): Float = 6.5F
        override fun allowSuicideMoves(): Boolean = false
    }

    private class TestingGameRulesSuicideAllowed : GoGameRulesHandler() {
        override fun getKomi(): Float = 6.5F
        override fun allowSuicideMoves(): Boolean = true
    }

    @Test
    fun `play first legal move has correct move tree state`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        Assertions.assertTimeout(Duration.ofSeconds(2)) {
            val result = model.playMove(0, 0)

            Assertions.assertEquals(result.moveNode!!, model.currentNode)
        }
    }

    @Test
    fun `play first legal move returns correct move submission result`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        Assertions.assertTimeout(Duration.ofSeconds(2)) {
            val moveSubmitResult = model.playMove(0, 0)

            Assertions.assertEquals(MoveValidationResult.OK, moveSubmitResult.validationResult)
            Assertions.assertNotNull(moveSubmitResult.moveNode)
            Assertions.assertTrue(moveSubmitResult.played)
        }
    }

    @Test
    fun `play first legal move resolves to correct board position`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        Assertions.assertTimeout(Duration.ofSeconds(2)) {
            val moveSubmitResult = model.playMove(0, 0)
            val newNode = moveSubmitResult.moveNode

            val firstMoveGameState = model.resolveGameState(newNode!!)
            val stoneColorAtPlayedMove = firstMoveGameState.gamePosition.getStoneColorAt(0, 0)

            Assertions.assertEquals(GoStoneColor.BLACK, stoneColorAtPlayedMove)
        }
    }

    @Test
    fun `play five consecutive moves all have correct stone color`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        Assertions.assertTimeout(Duration.ofSeconds(2)) {
            val moveSubmitResult = model.playMove(0, 0)
            val newNode = moveSubmitResult.moveNode

            val firstMoveGameState = model.resolveGameState(newNode!!)
            val stoneColorAtPlayedMove = firstMoveGameState.gamePosition.getStoneColorAt(0, 0)

            Assertions.assertEquals(GoStoneColor.BLACK, stoneColorAtPlayedMove)
        }
    }

    @Test
    fun `one stone capture works`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        Assertions.assertTimeout(Duration.ofSeconds(2)) {
            // Play black stone in the corner
            model.playMove(0, 0)

            // Play two white stones to capture it
            model.playMove(1, 0) // white removes a liberty
            model.playPass()
            model.playMove(0, 1) // white removes last liberty, stone at 0,0 should be captured now

            val gameState = model.resolveGameState(model.currentNode)
            Assertions.assertEquals(1, gameState.prisonersWhite)
            Assertions.assertEquals(GoStoneColor.NONE, gameState.gamePosition.getStoneColorAt(0, 0))
        }
    }

    @Test
    fun `connected string capture works with primary moves`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        Assertions.assertTimeout(Duration.ofSeconds(2)) {
            // Black plays three stones along the 1st column, white surrounds it on the 2nd column
            model.playMove(0, 0)
            model.playMove(1, 0)
            model.playMove(0, 1)
            model.playMove(1, 1)
            model.playMove(0, 2)
            model.playMove(1, 2) // After white plays this move, the three black stones on the left column should be all captured

            val currentState = model.resolveGameState(model.currentNode)

            Assertions.assertEquals(3, currentState.prisonersWhite)
            Assertions.assertEquals(GoStoneColor.NONE, currentState.gamePosition.getStoneColorAt(0, 0))
            Assertions.assertEquals(GoStoneColor.NONE, currentState.gamePosition.getStoneColorAt(0, 1))
            Assertions.assertEquals(GoStoneColor.NONE, currentState.gamePosition.getStoneColorAt(0, 2))
            Assertions.assertEquals(GoStoneColor.WHITE, currentState.gamePosition.getStoneColorAt(1, 0))
            Assertions.assertEquals(GoStoneColor.WHITE, currentState.gamePosition.getStoneColorAt(1, 1))
            Assertions.assertEquals(GoStoneColor.WHITE, currentState.gamePosition.getStoneColorAt(1, 2))
        }
    }

    @Test
    fun `ko recapture is illegal`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())

        Assertions.assertTimeout(Duration.ofSeconds(2)) {
            model.playMove(0, 0)
            model.playMove(1, 0)
            model.playMove(1, 1)
            model.playMove(0, 1) // white captures black at 0,0
            model.playMove(2, 0) // black sets up atari on 1,0 stone
            model.playPass() // white passes so black can capture 1,0 by playing at 0,0
            model.playMove(0, 0)

            // This move should now be illegal because 1,0 was just captured
            val submitResult = model.playMove(1, 0)

            Assertions.assertEquals(MoveValidationResult.ERROR_INVALID_KO_RECAPTURE, submitResult.validationResult)

            // TODO: Make sure that the state is correct, and that white did not play the capture

        }
    }

    @Test
    fun `board position repeat is illegal`() {

    }

    @Test
    fun `suicide works properly on one stone`() {

    }
}