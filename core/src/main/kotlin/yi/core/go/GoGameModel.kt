package yi.core.go

import yi.core.common.AbstractTreeBasedGameModel
import yi.core.common.GameNode
import yi.core.go.rules.GoGameRulesHandler
import java.util.*
import kotlin.collections.HashSet

/**
 * Represents one game of Go.
 *
 * //TODO: Explain the GameStateUpdate system
 * //TODO: Explain organisation internally, esp. how state is calculated and retrieved
 * //TODO: Explain how to submit moves
 */
class GoGameModel(val boardWidth: Int, val boardHeight: Int, val rules: GoGameRulesHandler, val stateHasher: GoStateHasher)
    : AbstractTreeBasedGameModel<GoGameStateUpdate>(GoGameStateUpdateFactory.createForRootNode(stateHasher.computeEmptyPositionHash(boardWidth, boardHeight))) {

    private var stateHashHistory: List<Long> = LinkedList()
    private var stateCache = WeakHashMap<Int, GoGameState>()

    init {
        if (boardWidth < 1 || boardHeight < 1)
            throw IllegalArgumentException("Invalid board dimensions: $boardWidth x $boardHeight")

        val emptyStateHash = stateHasher.computeEmptyPositionHash(boardWidth, boardHeight)

    }

    constructor(boardWidth: Int, boardHeight: Int, rulesHandler: GoGameRulesHandler) : this(boardWidth, boardHeight, rulesHandler, GoZobristHasher(boardWidth, boardHeight))

    constructor(boardWidth: Int, boardHeight: Int, rules: GoGameRules) : this(boardWidth, boardHeight, rules.getRulesHandler(), GoZobristHasher(boardWidth, boardHeight))

    /**
     * Returns a handler to play a series of moves in succession. This is the recommended approach when submitting multiple
     * moves to the game model with the assumption that each move must be submitted successfully.
     *
     * @see GoMoveSequence
     */
    fun beginMoveSequence(): GoMoveSequence {
        return GoMoveSequence(this)
    }

    /**
     * First checks if the move can be played at the current game position in compliance with the game rules. If successful,
     * appends a new node to the game tree.
     *
     * If the move is not compliant with the game rules, the method will fail silently without submitting any new node to
     * the game tree. To play a sequence of moves ensuring each move is played correctly, use [beginMoveSequence] instead.
     *
     * @return The result of the request. See [GoMoveSubmitResult] for more information.
     */
    fun playMove(x: Int, y: Int): GoMoveSubmitResult {
        val validationAndNewNode = GoMoveHelper.createMoveNodeForProposedMove(this, getCurrentMove(), true, GoStoneData(x, y, getNextTurnStoneColor()))

        val validationResult = validationAndNewNode.first
        val newNode: GameNode<GoGameStateUpdate>? = validationAndNewNode.second

        if (validationResult == GoMoveValidationResult.OK) {
            submitMove(newNode!!) // New node should not be null if validation result checks out
        }

        return GoMoveSubmitResult(validationResult, newNode, validationResult == GoMoveValidationResult.OK)
    }

    /**
     * Forcefully submit a move to the game tree without validating it against the game rules. Use this method with prudence, as
     * it may result in an erroneous game state.
     */
    fun playMoveIgnoringRules(x: Int, y: Int): GoMoveSubmitResult {
        val validationAndNewNode = GoMoveHelper.createMoveNodeForProposedMove(this, getCurrentMove(), false, GoStoneData(x, y, getNextTurnStoneColor()))
        val newNode: GameNode<GoGameStateUpdate>? = validationAndNewNode.second
        submitMove(newNode!!)

        return GoMoveSubmitResult(GoMoveValidationResult.OK, newNode, true)
    }

    /**
     * Checks if a hypothetical move can be played at the current game position. This will only test the move against the game rules, it will not create a node.
     *
     * @return [GoMoveValidationResult.OK] if the move can be played in compliance with game rules, otherwise other values representing
     *         reason for game rules violation.
     */
    fun validateMoveAgainstRules(game: GoGameModel, x: Int, y: Int): GoMoveValidationResult {
        val proposedMove = GoStoneData(x, y, game.getNextTurnStoneColor())
        val validationAndDelta = GoMoveHelper.validateProposedMoveAndCreateStateUpdate(game, game.getCurrentMove(), proposedMove)

        return validationAndDelta.first
    }

    fun playPass(): GoMoveSubmitResult {
        val newNode = GoMoveHelper.createMoveNodeForPass(this, getCurrentMove())
        submitMove(newNode)
        return GoMoveSubmitResult(GoMoveValidationResult.OK, newNode, true)
    }

    fun playResign(): GoMoveSubmitResult {
        val newNode = GoMoveHelper.createMoveNodeForResignation(this, getCurrentMove())
        submitMove(newNode)
        return GoMoveSubmitResult(GoMoveValidationResult.OK, newNode, true)
    }

    fun getCurrentGameState(): GoGameState {
        return getGameState(getCurrentMove())
    }

    fun getGameState(gameNode: GameNode<GoGameStateUpdate>): GoGameState {
        if (!gameTree.isDescendant(gameNode))
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
        var annotations = HashSet<GoAnnotation>();

        // Build the board state by traversing the history and apply the delta from root up to gameNode
        val pathToRoot = gameNode.getPathToRoot()

        pathToRoot.forEach { node ->
            node.data?.let { stateUpdate ->
                positionState.apply(stateUpdate)
                prisonersWhite += stateUpdate.captures.stream().filter { capture -> capture.stoneColor == GoStoneColor.BLACK }.count().toInt()
                prisonersBlack += stateUpdate.captures.stream().filter { capture -> capture.stoneColor == GoStoneColor.WHITE }.count().toInt()
                currentStateHash = stateUpdate.stateHash
            }
        }

        annotations = pathToRoot.last.data!!.annotationsOnThisNode;

        val gameState = GoGameState(this, positionState, gameNode, prisonersWhite, prisonersBlack, annotations, currentStateHash)
        this.stateCache[gameNode.getDistanceToRoot()] = gameState

        return gameState
    }

    fun addAnnotationOnThisMove(annotation: GoAnnotation) {
        addAnnotationsOnThisMove(annotation)
    }

    fun addAnnotationsOnThisMove(vararg annotations: GoAnnotation) {
        annotations.forEach { getCurrentMoveData()!!.annotationsOnThisNode.add(it) }
    }

    fun getAnnotationsOnThisMove(): Set<GoAnnotation> {
        return getCurrentMove().data!!.annotationsOnThisNode
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

    override fun onCurrentNodeUpdate(currentNode: GameNode<GoGameStateUpdate>) {
        val nodeHistory = currentNode.getPathToRoot()
        if (nodeHistory.size > 1) {
            // Only count non-root and primary move updates for unique state
            val uniqueStateHistory = nodeHistory.subList(1, nodeHistory.size)
                    .filter { state -> state.data!!.type == GoGameStateUpdate.Type.MOVE_PLAYED }

            this.stateHashHistory = uniqueStateHistory.map { item -> item.data!!.stateHash }
        }
    }
}