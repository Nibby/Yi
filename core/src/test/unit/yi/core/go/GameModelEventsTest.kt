package yi.core.go

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.TestGameRules.TestingGameRulesNoSuicide
import java.util.*

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
        model.onNodeAdd().addListener(eventListener)

        model.beginMoveSequence().playMove(0, 0)
        Thread.sleep(10)
        Assertions.assertTrue(eventReceived, "No event emitted")
    }

    @Test
    fun `play move generates onCurrentNodeChange() event`() {
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
        model.onCurrentNodeChange().addListener(eventListener)

        model.beginMoveSequence().playMove(0, 0)
        Thread.sleep(10)
        Assertions.assertTrue(eventReceived, "No event emitted")
    }

    @Test
    fun `delete move generates onNodeRemove() event`() {
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
        model.onNodeRemove().addListener(eventListener)

        val submitResult = model.submitMove(0, 0)
        model.removeNodeSubtree(submitResult.moveNode!!) // Has to be a legal move
        Thread.sleep(10)
        Assertions.assertTrue(eventReceived, "No event emitted")
    }

    @Test
    fun `change node data on non-current node generates onNodeDataUpdate() event`() {
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
        model.onNodeDataUpdate().addListener(eventListener)

        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)

        val thirdMove = model.submitMove(2, 0)

        // Sanity check
        Assertions.assertTrue(thirdMove.validationResult == MoveValidationResult.OK)
        Assertions.assertTrue(thirdMove.isPlayed())

        val nodeToEdit = thirdMove.moveNode!! // If the validation result is OK, then we should expect a non-null node here.
        model.setCurrentNode(model.getRootNode()) // Set current move somewhere that's not the last move
        model.addAnnotations(nodeToEdit, Collections.singleton(Annotation.Triangle(0, 0)))

        Thread.sleep(10)
        Assertions.assertTrue(eventReceived, "No event emitted")
    }

    @Test
    fun `change node data on current move generates onNodeDataUpdate() and onCurrentNodeDataUpdate() event`() {
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())
        var nodeDataUpdateEventReceived = false
        var currentNodeDataUpdateEventReceived = false

        // TODO: Either I am unaware of the language features or the lambda interop between Kotlin and Java
        //       needs some serious work.
        val eventListenerForCurrentNodeData = object : EventListener<NodeEvent> {
            override fun onEvent(event: NodeEvent) {
                currentNodeDataUpdateEventReceived = true
            }
        }

        val eventListenerForNodeData = object : EventListener<NodeEvent> {
            override fun onEvent(event: NodeEvent) {
                nodeDataUpdateEventReceived = true
            }
        }

        // Method under test
        model.onNodeDataUpdate().addListener(eventListenerForNodeData)
        model.onCurrentNodeDataUpdate().addListener(eventListenerForCurrentNodeData)

        model.addAnnotationToCurrentMove(Annotation.Triangle(0, 0))

        Thread.sleep(10)
        Assertions.assertTrue(nodeDataUpdateEventReceived, "No event emitted from onNodeDataUpdate()")
        Assertions.assertTrue(currentNodeDataUpdateEventReceived, "No event emitted from onCurrentNodeDataUpdate()")
    }

    @Test
    fun `remove parent node subtree fires onCurrentNodeChange() event`() {
        // We expect this event to be fired because the child node is part of the sub-tree that will be deleted.
        // So we adjust the current node to the new leaf node in the branch.
        val model = GameModel(3, 3, TestingGameRulesNoSuicide())
        var eventReceived = false

        val eventListener = object : EventListener<NodeEvent> {
            override fun onEvent(event: NodeEvent) {
                eventReceived = true
            }
        }

        model.onCurrentNodeChange().addListener(eventListener)

        // Do the action
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)
                .playMove(2, 0)
                .playMove(0, 1)
                .playMove(1, 1)

        model.toPreviousNode()
        val nodeToDelete = model.getCurrentNode()
        model.toNextNode() // Set current move to child of the node to delete
        model.removeNodeSubtree(nodeToDelete) // Should adjust the current node to the parent of nodeToDelete

        Thread.sleep(10)

        Assertions.assertTrue(eventReceived)
    }
}