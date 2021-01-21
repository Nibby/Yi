package yi.core.go.editor.edit

import yi.core.go.GameModel
import yi.core.go.GameNode

class RemoveNodeEdit constructor(private val nodeToRemove: GameNode) : GameModelEdit {

    private var parentOfNodeToRemove: GameNode? = null

    init {
        check(!nodeToRemove.isRoot()) { "Cannot remove root node" }
    }

    override fun rollbackChanges(model: GameModel) {
        check (parentOfNodeToRemove!!.isContinuationOf(model.getRootNode())) {
            "Attempting to undo but parent is no longer part of the game tree"
        }
        model.editor.addNode(parentOfNodeToRemove!!, nodeToRemove)
    }

    override fun performChanges(model: GameModel): Boolean {
        check(nodeToRemove.isContinuationOf(model.getRootNode())) {
            "Attempting to remove node but the node is no longer part of the game tree"
        }
        parentOfNodeToRemove = nodeToRemove.parent!!
        model.editor.removeNode(nodeToRemove)
        return true
    }

    override fun dispose() {
        nodeToRemove.dispose()
    }
}