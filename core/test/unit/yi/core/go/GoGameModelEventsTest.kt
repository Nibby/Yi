package yi.core.go

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.TestGameRules.TestingGameRulesNoSuicide

class GoGameModelEventsTest {

    @Test
    fun `append move generates onNodeAdd() event`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())
        var eventReceived = false

        // Method under test
        model.onNodeAdd().addObserver {
            eventReceived = true
        }

        model.beginMoveSequence().playMove(0, 0)
        Thread.sleep(10)
        Assertions.assertTrue(eventReceived)
    }

    @Test
    fun `play move generates onCurrentNodeUpdate() event`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())
        var eventReceived = false

        // Method under test
        model.onCurrentNodeUpdate().addObserver {
            eventReceived = true
        }

        model.beginMoveSequence().playMove(0, 0)
        Thread.sleep(10)
        Assertions.assertTrue(eventReceived)
    }

    @Test
    fun `delete move generates onNodeDelete() event`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())
        var eventReceived = false

        // Method under test
        model.onNodeDelete().addObserver {
            eventReceived = true
        }

        val submitResult = model.playMove(0, 0)
        model.deleteMove(submitResult.moveNode!!) // Has to be a legal move
        Thread.sleep(10)
        Assertions.assertTrue(eventReceived)
    }
}