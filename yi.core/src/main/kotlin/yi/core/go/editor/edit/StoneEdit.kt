package yi.core.go.editor.edit

import yi.core.go.*

sealed class StoneEdit(private var nodeToEdit: GameNode?) : GameModelEdit {

    private var createNewNode: Boolean = nodeToEdit == null
            || nodeToEdit!!.getType() != GameNodeType.STONE_EDIT
    private lateinit var parentOfNodeToEdit: GameNode

    override fun rollbackChanges(model: GameModel) {
        check (nodeToEdit != null) {
            "Attempting to undo but edit action has not been performed even once"
        }
        rollbackChangesNow(model, nodeToEdit!!)
        if (createNewNode) {
            model.editor.removeNodeSubtree(nodeToEdit!!)
        }
    }

    override fun performChanges(model: GameModel) {
        if (createNewNode) {
            if (nodeToEdit == null || nodeToEdit!!.getType() != GameNodeType.STONE_EDIT) {
                // First time submitting the node
                nodeToEdit = model.editor.addStoneEditNode()
            } else {
                // Subsequent re-do actions. Have to re-use this node because subsequent
                // edits also refer to this node when undo/redoing their changes.
                model.editor.addNode(parentOfNodeToEdit, nodeToEdit!!)
            }
        }
        if (!this::parentOfNodeToEdit.isInitialized) {
            parentOfNodeToEdit = nodeToEdit!!.parent!!
        }
        performChangesNow(model, nodeToEdit!!)
    }

    abstract fun rollbackChangesNow(model: GameModel, nodeToEdit: GameNode)
    abstract fun performChangesNow(model: GameModel, nodeToEdit: GameNode)

    class Add constructor(nodeToEdit: GameNode?, x: Int, y: Int, color: StoneColor)
        : StoneEdit(nodeToEdit) {

        private val stoneEditToAdd = Stone(x, y, color)

        override fun rollbackChangesNow(model: GameModel, nodeToEdit: GameNode) {
            assertStateCorrectBeforeRollback(nodeToEdit)
            model.editor.removeStoneEdit(nodeToEdit, stoneEditToAdd)
        }

        private fun assertStateCorrectBeforeRollback(nodeToEdit: GameNode) {
            val x = stoneEditToAdd.x
            val y = stoneEditToAdd.y
            val editHere: Stone? = nodeToEdit.getStoneEditAt(x, y)
            check (editHere != null) {
                "Attempting to rollback stone edit at ($x, $y) but no stone edit exists there"
            }
        }

        override fun performChangesNow(model: GameModel, nodeToEdit: GameNode) {
            assertStateCorrectBeforePerformingChanges(nodeToEdit)
            model.editor.addStoneEdit(nodeToEdit, stoneEditToAdd)
        }

        private fun assertStateCorrectBeforePerformingChanges(nodeToEdit: GameNode) {
            val x = stoneEditToAdd.x
            val y = stoneEditToAdd.y
            val editHere: Stone? = nodeToEdit.getStoneEditAt(x, y)
            check(editHere == null) {
                "Attempting to perform stone edit but a stone already exists at ($x, $y)"
            }
        }
    }

    class Remove constructor(nodeToEdit: GameNode?, private val x: Int, private val y: Int)
        : StoneEdit(nodeToEdit) {

        private lateinit var stoneEdit: Stone

        override fun rollbackChangesNow(model: GameModel, nodeToEdit: GameNode) {
            check (this::stoneEdit.isInitialized) {
                "No stone edit to remove. Ensure performChangesNow() has been called once first"
            }
            model.editor.addStoneEdit(nodeToEdit, stoneEdit)
        }

        override fun performChangesNow(model: GameModel, nodeToEdit: GameNode) {
            // The stone we wish to remove can either exist as a stone edit in this node delta
            // or in one of the parents... So there's no one size fit all solution.
            //
            // If there is an existing stone edit at this position, delete it.
            // Otherwise, create a new stone edit which sets the intersection to NONE
            val existingEditHere: Stone? = model.currentNode.getStoneEditAt(x, y)
            check (existingEditHere != null) {
                "No stone at ($x, $y) is available for removal on this node"
            }
            stoneEdit = existingEditHere
            model.editor.removeStoneEdit(nodeToEdit, stoneEdit)
        }

    }
}