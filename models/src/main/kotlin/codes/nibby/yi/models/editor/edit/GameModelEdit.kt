package codes.nibby.yi.models.editor.edit

import codes.nibby.yi.models.GameModel
import codes.nibby.yi.models.editor.GameModelEditException

/**
 * Represents an atomic data change operation to a [GameModel] that supports undo and
 * redo operations.
 */
interface GameModelEdit {

    /**
     * Request to un-apply the change requested by this edit. This should restore the
     * game position to the state before any changes were applied by this edit.
     *
     * @implNote Unlike [performChanges], this method assumes the operation will always
     * succeed. If that is not the case, throw a [IllegalStateException].
     */
    fun rollbackChanges(model: GameModel)

    /**
     * Request to (re-)apply the change represented by this edit.
     *
     * @implNote If the change could not be performed due to user input error, throw a
     * checked exception rather than returning unsuccessful. Only return false when
     * the change succeeds and does not need to be applied -- for example, attempting
     * to play a move when that move is already part of the continuation.
     *
     * @return Whether the changes have been successfully applied.
     */
    @Throws(GameModelEditException::class)
    fun performChanges(model: GameModel): Boolean

    /**
     * Notifies the edit is now discarded from the undo history, and that any temporary
     * data used to support undo/redo operation can be safely discarded.
     */
    fun dispose() {
        
    }
}