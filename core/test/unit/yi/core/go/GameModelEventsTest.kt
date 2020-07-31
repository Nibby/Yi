package yi.core.go

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.TestGameRules.TestingGameRulesNoSuicide

class GameModelEventsTest {

    @Test
    fun `append move generates onNodeAdd() event`() {
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())
        var eventReceived = false

        // TODO: Either I am unaware of the language features or the lambda interop between Kotlin and Java
        //       needs some serious work.
        val eventListener = object : EventListener<NodeEvent> {
            override fun onEvent(event: NodeEvent) {
                eventReceived = true
            }
        }

        // Method under test
        model.onNodeAdd().addListener(eventListener);

        model.beginMoveSequence().playMove(0, 0)
        Thread.sleep(10)
        Assertions.assertTrue(eventReceived, "No event emitted")
    }

    @Test
    fun `play move generates onCurrentNodeUpdate() event`() {
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())
        var eventReceived = false

        // TODO: Either I am unaware of the language features or the lambda interop between Kotlin and Java
        //       needs some serious work.
        val eventListener = object : EventListener<NodeEvent> {
            override fun onEvent(event: NodeEvent) {
                eventReceived = true
            }
        }

        // Method under test
        model.onCurrentNodeChange().addListener(eventListener);

        model.beginMoveSequence().playMove(0, 0)
        Thread.sleep(10)
        Assertions.assertTrue(eventReceived, "No event emitted")
    }

    @Test
    fun `delete move generates onNodeDelete() event`() {
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())
        var eventReceived = false

        // TODO: Either I am unaware of the language features or the lambda interop between Kotlin and Java
        //       needs some serious work.
        val eventListener = object : EventListener<NodeEvent> {
            override fun onEvent(event: NodeEvent) {
                eventReceived = true
            }
        }

        // Method under test
        model.onNodeRemove().addListener(eventListener);

        val submitResult = model.playMove(0, 0)
        model.removeNode(submitResult.moveNode!!) // Has to be a legal move
        Thread.sleep(10)
        Assertions.assertTrue(eventReceived, "No event emitted")
    }
}