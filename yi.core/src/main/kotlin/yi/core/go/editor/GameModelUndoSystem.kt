package yi.core.go.editor

import yi.core.go.GameModel
import yi.core.go.editor.edit.GameModelEdit
import yi.core.go.editor.edit.GameModelMergeableEdit
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.jvm.Throws

/**
 * Enables undoable changes to a [GameModel].
 *
 * @param model Game model to provide undo support to
 */
class GameModelUndoSystem(private val model: GameModel) {

    private val editHistory = Stack<GameModelEdit>()
    var maxHistorySize: Int = 50
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("Max history size must be >= 0")
            }
            editHistory.clear()
            field = value
        }

    var positionInHistory: Int = 0
        private set

    /**
     * @return true if the undo system can perform one more undo operation.
     */
    fun canUndo(): Boolean {
        return positionInHistory >= 0 && editHistory.size > 0
    }

    /**
     * @return true if the undo system can perform one more redo operation.
     */
    fun canRedo(): Boolean {
        return positionInHistory < editHistory.lastIndex
    }

    /**
     * Rollback the current [GameModelEdit] in the undo history. Caller must ensure another
     * undo operation is supported by invoking [canUndo].
     *
     * @throws IllegalStateException When [canUndo] returns false. In other words, when
     * no more undo operations can be performed.
     * @throws GameModelEditException When the undo edit failed to rollback the changes
     * successfully. This is usually a sign of internal state corruption.
     */
    fun performUndo() {
        if (!canUndo()) {
            throw IllegalStateException(
                "Current position in history does not support undo."
                        + " positionInHistory: " + positionInHistory
                        + ", editHistorySize: " + editHistory.size
            )
        }
        if (positionInHistory >= 0) {
            val editToUndo: GameModelEdit = editHistory[positionInHistory]
            editToUndo.rollbackChanges(model)
            positionInHistory--
        }
    }

    /**
     * Re-perform the next edit in edit history. Caller must ensure another redo operation
     * is supported by invoking [canRedo].
     *
     * @throws IllegalStateException When [canRedo] returns false. In other words, when
     * no more redo operations can be performed.
     * @throws GameModelEditException When the redo edit failed to apply the changes
     * successfully. This is usually a sign of internal state corruption.
     */
    fun performRedo() {
        if (!canRedo()) {
            throw IllegalStateException(
                "Current position in history does not support redo."
                        + " positionInHistory: " + positionInHistory
                        + ", editHistorySize: " + editHistory.size
            )
        }

        val editToApply = editHistory[positionInHistory + 1]
        editToApply.performChanges(model)
        positionInHistory++
    }

    /**
     * Wipes the stored undoable edit history. This operation will make all edits applied
     * to the [GameModel] so far undoable.
     */
    fun clearEditHistory() {
        editHistory.clear()
    }

    /**
     * @return Size of the current edit history
     */
    fun getEditHistorySize(): Int = editHistory.size

    /**
     * Records a new [GameModelEdit] to the edit history and adjusts the current undo
     * position to the new edit.
     *
     * If undo support is disabled, this method does nothing.
     */
    internal fun record(edit: GameModelEdit) {
        if (!mergeWithCurrentTopOfStackIfPossible(edit)) {
            pushChangeToEditHistory(edit)
        }
    }

    private fun mergeWithCurrentTopOfStackIfPossible(edit: GameModelEdit): Boolean {
        var actuallyMerged = false
        val canPossiblyMerge = !editHistory.isEmpty()
                && edit is GameModelMergeableEdit
                && editHistory.peek() is GameModelMergeableEdit

        if (canPossiblyMerge) {
            val mergeableEdit = edit as GameModelMergeableEdit
            val mergeableTopOfStack = editHistory.peek() as GameModelMergeableEdit
            if (mergeableTopOfStack.canMergeWith(mergeableEdit)) {
                mergeableTopOfStack.mergeWith(mergeableEdit)
                actuallyMerged = true
            }
        }

        return actuallyMerged
    }

    private fun pushChangeToEditHistory(edit: GameModelEdit) {
        discardEditsNewerThanCurrentPosition()
        editHistory.push(edit)
        positionInHistory = editHistory.indexOf(edit)
        pruneHistorySize()
    }

    private fun discardEditsNewerThanCurrentPosition() {
        if (positionInHistory < editHistory.size - 1) {
            // Discard the existing edit history
            val originalSize: Int = editHistory.size
            for (i in positionInHistory + 1 until originalSize) {
                editHistory.pop()
            }
        }
    }

    private fun pruneHistorySize() {
        while (editHistory.size > maxHistorySize) {
            editHistory.removeAt(0)
            --positionInHistory
        }
    }

    internal fun getHistoryItem(itemIndex: Int): GameModelEdit {
        return editHistory[itemIndex]
    }
}