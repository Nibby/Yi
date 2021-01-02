package yi.core.go.editor.edit

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.Annotation
import yi.core.go.Annotation.Triangle
import yi.core.go.GameModel
import yi.core.go.StandardGameRules
import java.util.*

class AnnotationEditTest {

    @Test
    fun `Submit add annotation edit creates history items`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val currentNode = model.currentNode
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        // Method under test
        val item1: AnnotationEdit = AnnotationEdit.forNew(currentNode, Triangle(0, 0), 0)
        val item2: AnnotationEdit = AnnotationEdit.forNew(currentNode, Triangle(1, 0), 1)
        val item3: AnnotationEdit = AnnotationEdit.forNew(currentNode, Triangle(2, 0), 2)

        undoableEditor.recordAndApplyUndoable(item1)
        undoableEditor.recordAndApplyUndoable(item2)
        undoableEditor.recordAndApplyUndoable(item3)

        Assertions.assertEquals(3, undoSystem.getEditHistorySize())
        Assertions.assertEquals(
            item1,
            undoSystem.getHistoryItem(0),
            "Incorrect undo stack state."
        )
        Assertions.assertEquals(
            item2,
            undoSystem.getHistoryItem(1),
            "Incorrect undo stack state."
        )
        Assertions.assertEquals(
            item3,
            undoSystem.getHistoryItem(2),
            "Incorrect undo stack state."
        )
    }

    @Test
    fun `Submit remove annotation edit creates history items`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val currentNode = model.currentNode
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        // Method under test
        val item1: AnnotationEdit = AnnotationEdit.forRemoval(currentNode, Triangle(0, 0), 0)
        val item2: AnnotationEdit = AnnotationEdit.forRemoval(currentNode, Triangle(1, 0), 1)
        val item3: AnnotationEdit = AnnotationEdit.forRemoval(currentNode, Triangle(2, 0), 2)

        undoableEditor.recordAndApplyUndoable(item1)
        undoableEditor.recordAndApplyUndoable(item2)
        undoableEditor.recordAndApplyUndoable(item3)

        Assertions.assertEquals(3, undoSystem.getEditHistorySize())
        Assertions.assertEquals(
            item1,
            undoSystem.getHistoryItem(0),
            "Incorrect undo stack state."
        )
        Assertions.assertEquals(
            item2,
            undoSystem.getHistoryItem(1),
            "Incorrect undo stack state."
        )
        Assertions.assertEquals(
            item3,
            undoSystem.getHistoryItem(2),
            "Incorrect undo stack state."
        )
    }

    @Test
    fun `Undo one annotation edit restores state correctly`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        val node = model.currentNode
        val annotations = node.getAnnotations()

        Assertions.assertTrue(
            annotations.isEmpty(),
            "Unexpected annotations on root node before test."
        )

        val item1: AnnotationEdit = AnnotationEdit.forNew(node, Triangle(0, 0), 0)
        undoableEditor.recordAndApplyUndoable(item1)
        Assertions.assertEquals(1, annotations.size)
        Assertions.assertEquals(Triangle(0, 0), annotations.iterator().next())

        // Method under test
        undoSystem.performUndo()

        // Assert
        Assertions.assertEquals(0, annotations.size)
    }

    @Test
    fun `Undo multiple annotations restore state correctly`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val currentNode = model.currentNode
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        // Same session, should be merged into one
        val item1: AnnotationEdit = AnnotationEdit.forNew(currentNode, Triangle(0, 0), 0)
        val item2: AnnotationEdit = AnnotationEdit.forNew(currentNode, Triangle(1, 0), 0)
        val item3: AnnotationEdit = AnnotationEdit.forNew(currentNode, Triangle(2, 0), 0)

        undoableEditor.recordAndApplyUndoable(item1)
        undoableEditor.recordAndApplyUndoable(item2)
        undoableEditor.recordAndApplyUndoable(item3)

        Assertions.assertEquals(1, undoSystem.getEditHistorySize(), "Incorrect history size.")
        Assertions.assertEquals(item1, undoSystem.getHistoryItem(0), "Incorrect undo stack state.")
        Assertions.assertTrue(undoSystem.canUndo(), "Cannot undo after merging three annotations edits into one")

        // Method under test
        undoSystem.performUndo()

        // Assert
        Assertions.assertTrue(model.getAnnotationsOnCurrentNode().isEmpty(),
            "Annotations are not cleared after performing undo.")
    }

    @Test
    fun `Undo consecutive one-annotation edits restore state correctly`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val currentNode = model.currentNode
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        val item1: AnnotationEdit = AnnotationEdit.forNew(currentNode, Triangle(0, 0), 0)
        val item2: AnnotationEdit = AnnotationEdit.forNew(currentNode, Triangle(1, 0), 1)
        val item3: AnnotationEdit = AnnotationEdit.forNew(currentNode, Triangle(2, 0), 2)

        undoableEditor.recordAndApplyUndoable(item1)
        undoableEditor.recordAndApplyUndoable(item2)
        undoableEditor.recordAndApplyUndoable(item3)

        Assertions.assertEquals(
            3,
            undoSystem.getEditHistorySize(),
            "Incorrect initial history size."
        )
        Assertions.assertEquals(
            item1,
            undoSystem.getHistoryItem(0),
            "Incorrect initial undo stack state."
        )
        Assertions.assertEquals(
            item2,
            undoSystem.getHistoryItem(1),
            "Incorrect initial undo stack state."
        )
        Assertions.assertEquals(
            item3,
            undoSystem.getHistoryItem(2),
            "Incorrect initial undo stack state."
        )
        Assertions.assertTrue(undoSystem.canUndo(), "Incorrect initial canUndo() state.")

        // Method under test (more below)
        undoSystem.performUndo()

        // Assert
        val node = model.currentNode
        val nodeAnnotations: Collection<Annotation?> = node.getAnnotations()

        // Use the annotation equality properties to our advantage
        var expectedAnnotations = HashSet<Annotation?>()
        expectedAnnotations.add(Triangle(0, 0))
        expectedAnnotations.add(Triangle(1, 0))
        Assertions.assertTrue(
            nodeAnnotations.containsAll(expectedAnnotations),
            "Incorrect annotation state after undo step 1."
        )
        undoSystem.performUndo()
        expectedAnnotations = HashSet()
        expectedAnnotations.add(Triangle(0, 0))
        Assertions.assertTrue(
            nodeAnnotations.containsAll(expectedAnnotations),
            "Incorrect annotation state after undo step 2."
        )
        undoSystem.performUndo()
        Assertions.assertTrue(
            nodeAnnotations.isEmpty(),
            "Annotations are not cleared after final undo."
        )
    }

    @Test
    fun `Consecutive undo+redo with multiple annotation addition and deletion restores state properly`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem
        val currentNode = model.currentNode

        undoSystem.maxHistorySize = 3

        val assertThirdEditState = Runnable {
            val annotations = model.getAnnotationsOnCurrentNode()
            Assertions.assertEquals(9, annotations.size)
            for (i in 0..8) {
                val x = i % 3
                val y = i / 3
                Assertions.assertTrue(annotations.contains(Annotation.Circle(x, y)))
            }
        }

        val assertSecondEditState = Runnable {
            val annotations = model.getAnnotationsOnCurrentNode()
            Assertions.assertEquals(4, annotations.size)
            Assertions.assertTrue(annotations.contains(Triangle(0, 0)))
            Assertions.assertTrue(annotations.contains(Triangle(2, 0)))
            Assertions.assertTrue(annotations.contains(Triangle(1, 1)))
            Assertions.assertTrue(annotations.contains(Triangle(1, 2)))
        }


        // First edit adds 3 annotations
        // xxx
        // ...
        // ...
        val edits: Stack<AnnotationEdit> = Stack()
        edits.push(AnnotationEdit.forNew(currentNode, Triangle(0, 0), 0))
        edits.push(AnnotationEdit.forNew(currentNode, Triangle(1, 0), 0))
        edits.push(AnnotationEdit.forNew(currentNode, Triangle(2, 0), 0))
        edits.forEach { undoableEditor.recordAndApplyUndoable(it) }

        // Second edit deletes 1 annotation and adds 2
        // x.x
        // .x.
        // .x.
        edits.clear()
        
        edits.push(AnnotationEdit.forRemoval(currentNode, Triangle(1, 0), 1))
        edits.push(AnnotationEdit.forNew(currentNode, Triangle(1, 1), 1))
        edits.push(AnnotationEdit.forNew(currentNode, Triangle(1, 2), 1))
        edits.forEach { undoableEditor.recordAndApplyUndoable(it) }
        assertSecondEditState.run()

        // Third edit overlays all annotations with another type
        // ooo
        // ooo
        // ooo
        edits.clear()
        edits.push(AnnotationEdit.forRemoval(currentNode, Triangle(1, 1), 2))
        edits.push(AnnotationEdit.forRemoval(currentNode, Triangle(1, 2), 2))
        edits.push(AnnotationEdit.forRemoval(currentNode, Triangle(0, 0), 2))
        edits.push(AnnotationEdit.forRemoval(currentNode, Triangle(2, 0), 2))
        edits.push(AnnotationEdit.forNew(currentNode, Annotation.Circle(0, 0), 2))
        edits.push(AnnotationEdit.forNew(currentNode, Annotation.Circle(1, 0), 2))
        edits.push(AnnotationEdit.forNew(currentNode, Annotation.Circle(2, 0), 2))
        edits.push(AnnotationEdit.forNew(currentNode, Annotation.Circle(0, 1), 2))
        edits.push(AnnotationEdit.forNew(currentNode, Annotation.Circle(1, 1), 2))
        edits.push(AnnotationEdit.forNew(currentNode, Annotation.Circle(2, 1), 2))
        edits.push(AnnotationEdit.forNew(currentNode, Annotation.Circle(0, 2), 2))
        edits.push(AnnotationEdit.forNew(currentNode, Annotation.Circle(1, 2), 2))
        edits.push(AnnotationEdit.forNew(currentNode, Annotation.Circle(2, 2), 2))
        edits.forEach { undoableEditor.recordAndApplyUndoable(it) }
        assertThirdEditState.run()

        Assertions.assertEquals(3, undoSystem.getEditHistorySize()) // 3 different sessions

        // Assertions to be used

        // Check initial annotations are correct
        assertThirdEditState.run()

        // Method under test
        Assertions.assertTrue(undoSystem.canUndo())
        undoSystem.performUndo()
        assertSecondEditState.run()
        Assertions.assertTrue(undoSystem.canUndo())
        undoSystem.performUndo()
        Assertions.assertTrue(undoSystem.canUndo())
        undoSystem.performUndo()
        Assertions.assertTrue(model.getAnnotationsOnCurrentNode().isEmpty())
        Assertions.assertFalse(undoSystem.canUndo())
        Assertions.assertTrue(undoSystem.canRedo())
        undoSystem.performRedo()
        Assertions.assertTrue(undoSystem.canRedo())
        undoSystem.performRedo()
        assertSecondEditState.run()
        Assertions.assertTrue(undoSystem.canRedo())
        undoSystem.performRedo()
        assertThirdEditState.run()
        Assertions.assertFalse(undoSystem.canRedo())
    }

    @Test
    fun `Many additions in one session are merged into most recent edit`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val currentNode = model.currentNode
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        val sessionId = 0L
        // Same session, should be merged into one
        val item1: AnnotationEdit = AnnotationEdit.forNew(currentNode, Triangle(0, 0), sessionId)
        val item2: AnnotationEdit = AnnotationEdit.forNew(currentNode, Triangle(1, 0), sessionId)
        val item3: AnnotationEdit = AnnotationEdit.forNew(currentNode, Triangle(2, 0), sessionId)

        undoableEditor.recordAndApplyUndoable(item1)
        undoableEditor.recordAndApplyUndoable(item2)
        undoableEditor.recordAndApplyUndoable(item3)


        Assertions.assertEquals(1, undoSystem.getEditHistorySize())
        Assertions.assertEquals(item1, undoSystem.getHistoryItem(0),
            "item2 and item3 should be merged into item1 because they are in the same session.")
    }

    @Test
    fun `Many removals in one session are merged into most recent edit`() {
        val model = GameModel(3, 3, StandardGameRules.CHINESE)
        val currentNode = model.currentNode
        val undoableEditor = model.editor
        val undoSystem = undoableEditor.undoSystem

        undoSystem.maxHistorySize = 3

        val sessionId = 0L
        // Same session, should be merged into one
        val item1: AnnotationEdit = AnnotationEdit.forRemoval(currentNode, Triangle(0, 0), sessionId)
        val item2: AnnotationEdit = AnnotationEdit.forRemoval(currentNode, Triangle(1, 0), sessionId)
        val item3: AnnotationEdit = AnnotationEdit.forRemoval(currentNode, Triangle(2, 0), sessionId)

        undoableEditor.recordAndApplyUndoable(item1)
        undoableEditor.recordAndApplyUndoable(item2)
        undoableEditor.recordAndApplyUndoable(item3)


        Assertions.assertEquals(1, undoSystem.getEditHistorySize())
        Assertions.assertEquals(item1, undoSystem.getHistoryItem(0),
            "item2 and item3 should be merged into item1 because they are in the same session.")
    }
}