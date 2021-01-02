package yi.core.go.editor.edit

import yi.core.go.GameModel
import yi.core.go.GameNode
import yi.core.go.Annotation

class AnnotationEdit(private val nodeToEdit: GameNode,
                     private val annotationsToAdd: HashSet<Annotation>,
                     private val annotationsToRemove: HashSet<Annotation>,
                     private val sessionId: Long) : GameModelMergeableEdit {


    override fun canMergeWith(anotherEdit: GameModelMergeableEdit): Boolean {
        return anotherEdit is AnnotationEdit && anotherEdit.sessionId == this.sessionId
    }

    override fun mergeWith(anotherEdit: GameModelMergeableEdit) {
        check(anotherEdit is AnnotationEdit) {
            "Can only merge with instances of ${AnnotationEdit::class.simpleName}"
        }
        anotherEdit.annotationsToAdd.forEach { toAdd ->
            this.annotationsToRemove.remove(toAdd)
            this.annotationsToAdd.add(toAdd)
        }
        anotherEdit.annotationsToRemove.forEach { toRemove ->
            this.annotationsToAdd.remove(toRemove)
            this.annotationsToRemove.add(toRemove)
        }
    }

    override fun rollbackChanges(model: GameModel) {
        model.editor.removeAnnotations(nodeToEdit, annotationsToAdd)
        model.editor.addAnnotations(nodeToEdit, annotationsToRemove)
    }

    override fun performChanges(model: GameModel) {
        model.editor.removeAnnotations(nodeToEdit, annotationsToRemove)
        model.editor.addAnnotations(nodeToEdit, annotationsToAdd)
    }

    companion object {
        fun forNew(nodeToEdit: GameNode, annotation: Annotation, sessionId: Long): AnnotationEdit {
            return AnnotationEdit(nodeToEdit, hashSetOf(annotation), HashSet(), sessionId)
        }

        fun forRemoval(nodeToEdit: GameNode, annotation: Annotation, sessionId: Long): AnnotationEdit {
            return AnnotationEdit(nodeToEdit, HashSet(), hashSetOf(annotation), sessionId)
        }
    }
}