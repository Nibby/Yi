package codes.nibby.yi.weiqi

import codes.nibby.yi.common.MoveNode
import codes.nibby.yi.common.MoveTree
import java.util.*

class GoGameModel(val boardWidth: Int, val boardHeight: Int, rulesHandler: GoGameRules) {

    val rules = rulesHandler.getRulesHandler()

    val moveTree = MoveTree<GameStateUpdate>()

    var currentNode = moveTree.rootNode
        set(value) {
            if (!moveTree.isDescendant(value))
                throw IllegalArgumentException("Node does not belong to the model game tree")

            this.stateHashHistory = value.getPathToRoot().map { item -> item.data!!.stateHash }
        }

    val stateHasher: StateHasher = ZobristHasher(boardWidth, boardHeight)

    private var stateHashHistory: List<Long> = LinkedList()

    init {
        moveTree.rootNode.data = GameStateUpdateFactory.createRootUpdate()
    }

    fun appendNewNode(newNode: MoveNode<GameStateUpdate>) {
        moveTree.appendNode(currentNode, newNode)
    }

    fun resolveGameState(gameNode: MoveNode<GameStateUpdate>): GoGameState {
        val positionState = GoGamePosition(boardWidth, boardHeight)

        var prisonersWhite = 0
        var prisonersBlack = 0

        var currentStateHash = 0L

        // Build the board state by traversing the history and apply the delta from root up to gameNode
        gameNode.getPathToRoot().forEach { node ->
            node.data?.let { delta ->
                positionState.apply(delta)
                prisonersBlack += delta.captures.stream().filter { capture -> capture.stoneColor == GoStoneColor.BLACK }.count().toInt()
                prisonersWhite += delta.captures.stream().filter { capture -> capture.stoneColor == GoStoneColor.WHITE }.count().toInt()

                val updateHash = when {
                    node.isRoot() -> node.data!!.stateHash
                    else -> stateHasher.calculateUpdateHash(node.parent!!.data!!.stateHash, delta.getStoneUpdates())
                }
                currentStateHash = currentStateHash xor updateHash
            }
        }

        return GoGameState(this, positionState, gameNode, prisonersWhite, prisonersBlack, currentStateHash)
    }

    fun getIntersectionCount() = boardWidth * boardHeight

}