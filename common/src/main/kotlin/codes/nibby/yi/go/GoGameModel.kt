package codes.nibby.yi.go

import codes.nibby.yi.common.MoveNode
import codes.nibby.yi.common.MoveTree
import java.util.*

class GoGameModel(val boardWidth: Int, val boardHeight: Int, val rules: GoGameRulesHandler) {

    internal val moveTree = MoveTree<GameStateUpdate>()

    internal var currentNode = moveTree.rootNode
        set(value) {
            if (!moveTree.isDescendant(value))
                throw IllegalArgumentException("Node does not belong to the model game tree")

            this.stateHashHistory = value.getPathToRoot().map { item -> item.data!!.stateHash }
            field = value
        }

    internal val stateHasher: StateHasher = ZobristHasher(boardWidth, boardHeight)

    private var stateHashHistory: List<Long> = LinkedList()

    init {
        moveTree.rootNode.data = GameStateUpdateFactory.createForRootNode(stateHasher.getEmptyStateHash())
    }

    constructor(boardWidth: Int, boardHeight: Int, rules: GoGameRules) : this(boardWidth, boardHeight, rules.getRulesHandler())

    /**
     * Forcefully submit a move to the game tree without validating it against the game rules. Use this method with prudence, as
     * it may result in an erroneous game state.
     */
    fun playMoveIgnoringRules(x: Int, y: Int): MoveSubmitResult {
        val validationAndNewNode = GoMoveHelper.createMoveNodeForProposedMove(this, currentNode, false, StoneData(x, y, getNextTurnStoneColor()))
        val newNode: MoveNode<GameStateUpdate>? = validationAndNewNode.second
        submitMoveNode(newNode!!)

        return MoveSubmitResult(MoveValidationResult.OK, newNode, true)
    }

    /**
     * First checks if the move can be played at the current game position in compliance with the game rules. If successful,
     * appends a new node to the game tree.
     *
     * @return The result of the request. See [MoveSubmitResult] for more information.
     */
    fun playMove(x: Int, y: Int): MoveSubmitResult {
        val validationAndNewNode = GoMoveHelper.createMoveNodeForProposedMove(this, currentNode, true, StoneData(x, y, getNextTurnStoneColor()))

        val validationResult = validationAndNewNode.first
        val newNode: MoveNode<GameStateUpdate>? = validationAndNewNode.second

        if (validationResult == MoveValidationResult.OK) {
            submitMoveNode(newNode!!) // New node should not be null if validation result checks out
        }
        return MoveSubmitResult(validationResult, newNode, validationResult == MoveValidationResult.OK)
    }

    /**
     * Checks if a hypothetical move can be played at the current game position. This will only test the move against the game rules, it will not create a node.
     *
     * @return [MoveValidationResult.OK] if the move can be played in compliance with game rules, otherwise other values representing
     *         reason for game rules violation.
     */
    fun validateMoveAgainstRules(game: GoGameModel, x: Int, y: Int): MoveValidationResult {
        val proposedMove = StoneData(x, y, game.getNextTurnStoneColor())
        val validationAndDelta = GoMoveHelper.validateProposedMoveAndCreateStateUpdate(game, game.currentNode, proposedMove)

        return validationAndDelta.first
    }

    fun playPass(): MoveSubmitResult {
        val newNode = GoMoveHelper.createMoveNodeForPass(this, currentNode)
        submitMoveNode(newNode)
        return MoveSubmitResult(MoveValidationResult.OK, newNode, true)
    }

    fun playResign(): MoveSubmitResult {
        val newNode = GoMoveHelper.createMoveNodeForResignation(this, currentNode)
        submitMoveNode(newNode)
        return MoveSubmitResult(MoveValidationResult.OK, newNode, true)
    }

    fun resolveGameState(gameNode: MoveNode<GameStateUpdate>): GoGameState {
        val positionState = GoGamePosition(boardWidth, boardHeight)

        var prisonersWhite = 0
        var prisonersBlack = 0

        var currentStateHash = stateHasher.getEmptyStateHash()

        // Build the board state by traversing the history and apply the delta from root up to gameNode
        gameNode.getPathToRoot().forEach { node ->
            node.data?.let { stateUpdate ->
                positionState.apply(stateUpdate)
                prisonersWhite += stateUpdate.captures.stream().filter { capture -> capture.stoneColor == GoStoneColor.BLACK }.count().toInt()
                prisonersBlack += stateUpdate.captures.stream().filter { capture -> capture.stoneColor == GoStoneColor.WHITE }.count().toInt()
                currentStateHash = stateUpdate.stateHash
            }
        }

        return GoGameState(this, positionState, gameNode, prisonersWhite, prisonersBlack, currentStateHash)
    }

    /**
     * Appends the move node after the current position in the game tree. If the
     * node has not been validated by [validateMoveAgainstRules], it may corrupt the game state.
     */
    private fun submitMoveNode(newNode: MoveNode<GameStateUpdate>) {
        appendNewNode(newNode)
        currentNode = newNode
    }

    fun getCurrentMoveNumber(): Int {
        return currentNode.getDistanceToRoot()
    }

    fun getNextMoveNumber(): Int {
        return getCurrentMoveNumber() + 1
    }

    fun getNextTurnStoneColor(): GoStoneColor {
        return rules.getStoneColorForTurn(getNextMoveNumber())
    }

    private fun appendNewNode(newNode: MoveNode<GameStateUpdate>) {
        moveTree.appendNode(currentNode, newNode)
    }

    fun getIntersectionCount() = boardWidth * boardHeight

}