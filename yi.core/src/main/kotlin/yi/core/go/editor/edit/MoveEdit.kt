package yi.core.go.editor.edit

import yi.core.go.GameModel
import yi.core.go.GameNode
import yi.core.go.MoveSubmitResult
import yi.core.go.MoveValidationResult

/**
 * Represents an undoable game move.
 */
class MoveEdit private constructor(private val moveX: Int,
                           private val moveY: Int,
                           private val editType: EditType) : GameModelEdit {

    enum class EditType {
        PlayedMove,
        Pass
    }

    private var parentOfSubmittedNode: GameNode? = null
    var submittedNode: GameNode? = null
        private set
    var moveValidationResult : MoveValidationResult? = null
        private set

    override fun rollbackChanges(model: GameModel) {
        model.editor.removeNodeSubtree(submittedNode!!)
        model.currentNode = parentOfSubmittedNode!!
    }

    override fun performChanges(model: GameModel): Boolean {
        if (submittedNode == null) {
            return submitMoveForFirstTimeEdit(model)
        }

        appendSubmittedNodeBackToGameTree(model)
        return true
    }

    private fun submitMoveForFirstTimeEdit(model: GameModel): Boolean {
        val currentMoveBeforeNewMoveSubmission: GameNode = model.currentNode

        val result: MoveSubmitResult = when (editType) {
            EditType.PlayedMove -> model.editor.addMove(moveX, moveY)
            EditType.Pass -> model.editor.addPass()
        }

        moveValidationResult = result.validationResult

        // Do not record this edit if we are re-using an existing node because technically
        // our change doesn't count as a new "edit", so attempting to undo this change
        // will corrupt the edit history.

        // Do not record this edit if we are re-using an existing node because technically
        // our change doesn't count as a new "edit", so attempting to undo this change
        // will corrupt the edit history.
        if (moveValidationResult !== MoveValidationResult.OK || result.isReusingExistingNode) {
            return false
        }

        check(result.isPlayed) {
            "Move is not played internally, is it returning " +
                    "the result before being submitted to the game tree?"
        }

        submittedNode = result.moveNode
        parentOfSubmittedNode = currentMoveBeforeNewMoveSubmission
        return true
    }

    private fun appendSubmittedNodeBackToGameTree(model: GameModel) {
        model.editor.addNode(parentOfSubmittedNode!!, submittedNode!!)
    }

    internal fun getSubmittedNode(): GameNode? {
        return submittedNode
    }

    companion object {
        fun playedMove(x: Int, y: Int):  MoveEdit = MoveEdit(x, y, EditType.PlayedMove)
        fun pass(): MoveEdit = MoveEdit(-1, -1, EditType.Pass)
    }
}