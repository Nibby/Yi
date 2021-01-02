package yi.core.go.editor.edit

/**
 * A [GameModelEdit] that supports merging contents with another edit of the same type.
 *
 * Some atomic operations, when performed in rapid succession, may be considered a
 * single undoable action. Examples include bulk-adding annotations to one game node.
 */
interface GameModelMergeableEdit : GameModelEdit {

    /**
     * Determines whether this edit can be merged with another edit.
     *
     * @return true if the two edits are mergeable.
     */
    fun canMergeWith(anotherEdit: GameModelMergeableEdit): Boolean

    /**
     * Merge the edits made in another edit with this edit.
     */
    fun mergeWith(anotherEdit: GameModelMergeableEdit)
}