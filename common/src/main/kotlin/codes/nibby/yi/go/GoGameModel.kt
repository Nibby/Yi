package codes.nibby.yi.go

import codes.nibby.yi.common.MoveNode
import codes.nibby.yi.common.MoveTree
import codes.nibby.yi.go.rules.GoGameRulesHandler
import java.util.*

/**
 * Represents one game of Go.
 *
 * //TODO: Explain the GameStateUpdate system
 * //TODO: Explain organisation internally, esp. how state is calculated and retrieved
 * //TODO: Explain how to submit moves
 */
class GoGameModel(val boardWidth: Int, val boardHeight: Int, val rules: GoGameRulesHandler, val stateHasher: StateHasher) {

    internal val moveTree = MoveTree<GameStateUpdate>()

    internal var currentNode = moveTree.rootNode
        set(value) {
            if (!moveTree.isDescendant(value))
                throw IllegalArgumentException("Node does not belong to the model game tree")

            field = value
            onCurrentNodeUpdate(currentNode)
        }

    private var stateHashHistory: List<Long> = LinkedList()
    private var stateCache = WeakHashMap<Int, GoGameState>()

    init {
        val emptyStateHash = stateHasher.computeEmptyPositionHash(boardWidth, boardHeight)
        moveTree.rootNode.data = GameStateUpdateFactory.createForRootNode(emptyStateHash)
    }

    constructor(boardWidth: Int, boardHeight: Int, rulesHandler: GoGameRulesHandler) : this(boardWidth, boardHeight, rulesHandler, ZobristHasher(boardWidth, boardHeight))

    constructor(boardWidth: Int, boardHeight: Int, rules: GoGameRules) : this(boardWidth, boardHeight, rules.getRulesHandler(), ZobristHasher(boardWidth, boardHeight))

    /**
     * Returns a handler to play a series of moves in succession. This is the recommended approach when submitting multiple
     * moves to the game model with the assumption that each move must be submitted successfully.
     *
     * @see MoveSequence
     */
    fun beginMoveSequence(): MoveSequence {
        return MoveSequence(this)
    }

    /**
     * First checks if the move can be played at the current game position in compliance with the game rules. If successful,
     * appends a new node to the game tree.
     *
     * If the move is not compliant with the game rules, the method will fail silently without submitting any new node to
     * the game tree. To play a sequence of moves ensuring each move is played correctly, use [beginMoveSequence] instead.
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

    fun getCurrentGameState(): GoGameState {
        return getGameState(currentNode)
    }

    fun getGameState(gameNode: MoveNode<GameStateUpdate>): GoGameState {
        if (!moveTree.isDescendant(gameNode))
            throw IllegalArgumentException("Game node is not part of this move tree")

        val moveNumber = gameNode.getDistanceToRoot()

        this.stateCache[moveNumber]?.let {
            return it
        }

        // Only perform state resolution if we don't have a cached position
        val positionState = GoGamePosition(boardWidth, boardHeight)

        var prisonersWhite = 0
        var prisonersBlack = 0

        var currentStateHash = stateHasher.computeEmptyPositionHash(boardWidth, boardHeight)

        // Build the board state by traversing the history and apply the delta from root up to gameNode
        gameNode.getPathToRoot().forEach { node ->
            node.data?.let { stateUpdate ->
                positionState.apply(stateUpdate)
                prisonersWhite += stateUpdate.captures.stream().filter { capture -> capture.stoneColor == GoStoneColor.BLACK }.count().toInt()
                prisonersBlack += stateUpdate.captures.stream().filter { capture -> capture.stoneColor == GoStoneColor.WHITE }.count().toInt()
                currentStateHash = stateUpdate.stateHash
            }
        }

        val gameState = GoGameState(this, positionState, gameNode, prisonersWhite, prisonersBlack, currentStateHash)
        this.stateCache[gameNode.getDistanceToRoot()] = gameState

        return gameState
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

    fun getIntersectionCount() = boardWidth * boardHeight

    /**
     * Returns a copy of the state hash history that led up to the current state.
     */
    fun getStateHashHistory(): List<Long> {
        return ArrayList(stateHashHistory)
    }

    /**
     * Appends the move node after the current position in the game tree. If the
     * node has not been validated by [validateMoveAgainstRules], it may corrupt the game state.
     */
    private fun submitMoveNode(newNode: MoveNode<GameStateUpdate>) {
        appendNewNode(newNode)
        currentNode = newNode
    }

    private fun appendNewNode(newNode: MoveNode<GameStateUpdate>) {
        moveTree.appendNode(currentNode, newNode)
    }

    private fun onCurrentNodeUpdate(currentNode: MoveNode<GameStateUpdate>) {
        val nodeHistory = currentNode.getPathToRoot()
        if (nodeHistory.size > 1) {
            // Only count non-root and primary move updates for unique state
            val uniqueStateHistory = nodeHistory.subList(1, nodeHistory.size)
                                                .filter { state -> state.data!!.type == GameStateUpdate.Type.MOVE_PLAYED }

            this.stateHashHistory = uniqueStateHistory.map { item -> item.data!!.stateHash }
        }
    }
}