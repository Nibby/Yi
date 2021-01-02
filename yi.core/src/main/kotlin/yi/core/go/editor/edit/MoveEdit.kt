package yi.core.go.editor.edit

import yi.core.go.GameModel
import yi.core.go.GameNode
import yi.core.go.MoveSubmitResult
import yi.core.go.MoveValidationResult

/**
 * Represents an undoable game move.
 */
class MoveEdit constructor(private val moveX: Int,
                           private val moveY: Int) : GameModelEdit {

    private var parentOfSubmittedNode: GameNode? = null
    var submittedNode: GameNode? = null
        private set
    var moveValidationResult : MoveValidationResult? = null
        private set

    override fun rollbackChanges(model: GameModel) {
        model.editor.removeNodeSubtree(submittedNode!!)
        model.currentNode = parentOfSubmittedNode!!
    }

    override fun performChanges(model: GameModel) {
        if (submittedNode == null) {
            submitMoveForFirstTimeEdit(model)
            return
        }

        appendSubmittedNodeBackToGameTree(model)
    }

    private fun submitMoveForFirstTimeEdit(model: GameModel) {
        val currentMoveBeforeNewMoveSubmission: GameNode = model.currentNode

        val moveSubmitResult: MoveSubmitResult = model.editor.addMove(moveX, moveY)
        moveValidationResult = moveSubmitResult.validationResult

        // Do not record this edit if we are re-using an existing node because technically
        // our change doesn't count as a new "edit", so attempting to undo this change
        // will corrupt the edit history.

        // Do not record this edit if we are re-using an existing node because technically
        // our change doesn't count as a new "edit", so attempting to undo this change
        // will corrupt the edit history.
        if (moveValidationResult !== MoveValidationResult.OK || moveSubmitResult.isReusingExistingNode) {
            return
        }

        check(moveSubmitResult.isPlayed) {
            "Move is not played internally, is it returning " +
                    "the result before being submitted to the game tree?"
        }

        submittedNode = moveSubmitResult.moveNode
        parentOfSubmittedNode = currentMoveBeforeNewMoveSubmission
    }

    private fun appendSubmittedNodeBackToGameTree(model: GameModel) {
        model.editor.addNode(parentOfSubmittedNode!!, submittedNode!!)
    }

    internal fun getSubmittedNode(): GameNode? {
        return submittedNode
    }
}