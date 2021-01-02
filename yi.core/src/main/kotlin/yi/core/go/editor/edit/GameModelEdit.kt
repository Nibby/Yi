package yi.core.go.editor.edit

import yi.core.go.GameModel
import yi.core.go.editor.GameModelEditException

/**
 * Represents an atomic data change operation to a [GameModel] that supports undo and
 * redo operations.
 */
interface GameModelEdit {

    /**
     * Request to un-apply the change requested by this edit.
     */
    fun rollbackChanges(model: GameModel)

    /**
     * Request to (re-)apply the change represented by this edit.
     */
    @Throws(GameModelEditException::class)
    fun performChanges(model: GameModel)
}