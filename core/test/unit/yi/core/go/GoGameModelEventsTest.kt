package yi.core.go

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.common.EventListener
import yi.core.common.NodeEvent
import yi.core.go.TestGameRules.TestingGameRulesNoSuicide
import java.util.*

class GoGameModelEventsTest {

    @Test
    fun `append move generates onNodeAdd() event`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())
        var eventReceived = false

        // TODO: Either I am unaware of the language features or the lambda interop between Kotlin and Java
        //       needs some serious work.
        val eventListener = object : EventListener<NodeEvent<GoGameStateUpdate>> {
            override fun onEvent(event: NodeEvent<GoGameStateUpdate>) {
                eventReceived = true
            }
        }

        // Method under test
        model.onNodeAdd().addListener(eventListener);

        model.beginMoveSequence().playMove(0, 0)
        Thread.sleep(10)
        Assertions.assertTrue(eventReceived)
    }

    @Test
    fun `play move generates onCurrentNodeUpdate() event`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())
        var eventReceived = false

        // TODO: Either I am unaware of the language features or the lambda interop between Kotlin and Java
        //       needs some serious work.
        val eventListener = object : EventListener<NodeEvent<GoGameStateUpdate>> {
            override fun onEvent(event: NodeEvent<GoGameStateUpdate>) {
                eventReceived = true
            }
        }

        // Method under test
        model.onCurrentNodeUpdate().addListener(eventListener);

        model.beginMoveSequence().playMove(0, 0)
        Thread.sleep(10)
        Assertions.assertTrue(eventReceived)
    }

    @Test
    fun `delete move generates onNodeDelete() event`() {
        val model = GoGameModel(3, 3, TestingGameRulesNoSuicide())
        var eventReceived = false

        // TODO: Either I am unaware of the language features or the lambda interop between Kotlin and Java
        //       needs some serious work.
        val eventListener = object : EventListener<NodeEvent<GoGameStateUpdate>> {
            override fun onEvent(event: NodeEvent<GoGameStateUpdate>) {
                eventReceived = true
            }
        }

        // Method under test
        model.onNodeDelete().addListener(eventListener);

        val submitResult = model.playMove(0, 0)
        model.deleteMove(submitResult.moveNode!!) // Has to be a legal move
        Thread.sleep(10)
        Assertions.assertTrue(eventReceived)
    }
}