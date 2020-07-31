@file:Suppress("MemberVisibilityCanBePrivate") // Some methods may not be used, but is intended to be part of the public API

package yi.core.go

import yi.core.go.rules.GoGameRulesHandler
import java.util.*

/**
 * Primary model object representing one game of Go. The model is designed to be state-based --
 * that there is always a current 'position' at any point in time.
 *
 * Unlike the [GameTree], methods in this model should be more domain-specific.
 *
 * //TODO: Explain the GameStateUpdate system
 * //TODO: Explain organisation internally, esp. how state is calculated and retrieved
 * //TODO: Explain how to submit moves
 */
class GameModel(val boardWidth: Int, val boardHeight: Int, val rules: GoGameRulesHandler, val stateHasher: GameStateHasher) {

    internal val gameTree = GameTree()
    private var _currentMove = gameTree.rootNode
        set(value) {
            if (!gameTree.isDescendant(value))
                throw IllegalArgumentException("Node does not belong to the model game tree")

            field = value
            internalCurrentNodeUpdate(field)
        }

    init {
        gameTree.rootNode.stateDelta = StateDelta.forRootNode(stateHasher.computeEmptyPositionHash(boardWidth, boardHeight))
    }

    private var stateHashHistory: List<Long> = LinkedList()
    private var stateCache = WeakHashMap<Long, GameState>()

    init {
        if (boardWidth < 1 || boardHeight < 1)
            throw IllegalArgumentException("Invalid board dimensions: $boardWidth x $boardHeight")
    }

    constructor(boardWidth: Int, boardHeight: Int, rulesHandler: GoGameRulesHandler) : this(boardWidth, boardHeight, rulesHandler, ZobristHasher(boardWidth, boardHeight))

    constructor(boardWidth: Int, boardHeight: Int, rules: GameRules) : this(boardWidth, boardHeight, rules.getRulesHandler(), ZobristHasher(boardWidth, boardHeight))


    private fun internalCurrentNodeUpdate(currentNode: GameNode) {
        val nodeHistory = currentNode.getMoveHistory()
        if (nodeHistory.size > 1) {
            // Only count non-root and primary move updates for unique state
            val uniqueStateHistory = nodeHistory.subList(1, nodeHistory.size)
                    .filter { state -> state.stateDelta.type == StateDelta.Type.MOVE_PLAYED }

            this.stateHashHistory = uniqueStateHistory.map { item -> item.stateDelta.stateHash }
        }
    }

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
     * If there is another move that is already played at the same co-ordinates following the current move, then no new
     * node will be submitted to the game tree. Instead, the current position will be set to that move.
     *
     * @return The result of the request. See [MoveSubmitResult] for more information.
     */
    fun playMove(x: Int, y: Int): MoveSubmitResult {
        var identicalExistingMove: GameNode? = null

        for (child in getCurrentMove().children) {
            child.stateDelta.primaryMove?.let {
                val moveX = it.x
                val moveY = it.y

                if (moveX == x && moveY == y) {
                    identicalExistingMove = child
                }
            }

            if (identicalExistingMove != null)
                break
        }

        val validationResult: MoveValidationResult
        val node: GameNode?
        val movePlayed: Boolean

        if (identicalExistingMove == null) {
            val validationAndNewNode = GameMoveSubmitter.createMoveNodeForProposedMove(this, getCurrentMove(), true, Stone(x, y, getNextTurnStoneColor()))

            validationResult = validationAndNewNode.first
            node = validationAndNewNode.second

            if (validationResult == MoveValidationResult.OK) {
                submitMove(node!!) // New node should not be null if validation result checks out
            }

            movePlayed = validationResult == MoveValidationResult.OK
        } else {
            setCurrentMove(identicalExistingMove!!)

            validationResult = MoveValidationResult.OK
            node = identicalExistingMove
            movePlayed = true
        }

        return MoveSubmitResult(validationResult, node, movePlayed)
    }

    /**
     * Forcefully submit a move to the game tree without validating it against the game rules. Use this method with prudence, as
     * it may result in an erroneous game state.
     */
    fun playMoveIgnoringRules(x: Int, y: Int): MoveSubmitResult {
        val validationAndNewNode = GameMoveSubmitter.createMoveNodeForProposedMove(this, getCurrentMove(), false, Stone(x, y, getNextTurnStoneColor()))
        val newNode: GameNode? = validationAndNewNode.second
        submitMove(newNode!!)

        return MoveSubmitResult(MoveValidationResult.OK, newNode, true)
    }

    /**
     * Checks if a hypothetical move can be played at the current game position. This will only test the move against the game rules, it will not create a node.
     *
     * @return [MoveValidationResult.OK] if the move can be played in compliance with game rules, otherwise other values representing
     *         reason for game rules violation.
     */
    fun validateMoveAgainstRules(game: GameModel, x: Int, y: Int): MoveValidationResult {
        val proposedMove = Stone(x, y, game.getNextTurnStoneColor())
        val validationAndDelta = GameMoveSubmitter.validateProposedMoveAndCreateStateUpdate(game, game.getCurrentMove(), proposedMove)

        return validationAndDelta.first
    }

    /**
     * Play a pass for the next turn.
     */
    fun playPass(): MoveSubmitResult {
        val newNode = GameMoveSubmitter.createMoveNodeForPass(getCurrentMove())
        submitMove(newNode)
        return MoveSubmitResult(MoveValidationResult.OK, newNode, true)
    }

    /**
     * Resign the game for next turn.
     */
    fun playResign(): MoveSubmitResult {
        val newNode = GameMoveSubmitter.createMoveNodeForResignation(getCurrentMove())
        submitMove(newNode)
        return MoveSubmitResult(MoveValidationResult.OK, newNode, true)
    }

    /**
     * Equivalent to invoking [getGameState] at [getCurrentMove].
     *
     * @return The [GameState] at the current position.
     */
    fun getCurrentGameState(): GameState {
        return getGameState(getCurrentMove())
    }

    /**
     * The game state is dynamically calculated if it does not exist, otherwise it will be cached for a period of time for performance.
     *
     * @return The [GameState] at a given node position.
     */
    fun getGameState(gameNode: GameNode): GameState {
        if (!gameTree.isDescendant(gameNode))
            throw IllegalArgumentException("Game node is not part of this move tree")

        this.stateCache[gameNode.stateDelta.stateHash]?.let {
            return it
        }

        // Only perform state resolution if we don't have a cached position
        val positionState = GamePosition(boardWidth, boardHeight)

        var prisonersWhite = 0
        var prisonersBlack = 0

        var currentStateHash = stateHasher.computeEmptyPositionHash(boardWidth, boardHeight)
        val annotations: HashSet<Annotation>

        // Build the board state by traversing the history and apply the delta from root up to gameNode
        val pathToRoot = gameNode.getMoveHistory()

        pathToRoot.forEach { node ->
            node.stateDelta.let { stateUpdate ->
                positionState.apply(stateUpdate)
                prisonersWhite += stateUpdate.captures.stream().filter { capture -> capture.stoneColor == StoneColor.BLACK }.count().toInt()
                prisonersBlack += stateUpdate.captures.stream().filter { capture -> capture.stoneColor == StoneColor.WHITE }.count().toInt()
                currentStateHash = stateUpdate.stateHash
            }
        }

        annotations = pathToRoot.last.stateDelta.annotationsOnThisNode

        val gameState = GameState(this, positionState, gameNode, prisonersWhite, prisonersBlack, annotations, currentStateHash)
        this.stateCache[gameNode.stateDelta.stateHash] = gameState

        return gameState
    }

    /**
     * Adds one [Annotation] to the current move.
     *
     * This method emits an [onCurrentNodeDataUpdate] event.
     */
    fun addAnnotationToCurrentMove(annotation: Annotation) {
        addAnnotationsToCurrentMove(Collections.singleton(annotation))
    }

    /**
     * Calls [addAnnotations] using the current move.
     */
    fun addAnnotationsToCurrentMove(annotations: Collection<Annotation>) {
        addAnnotations(getCurrentMove(), annotations);
    }

    /**
     * Adds one or more [Annotation] to the current move.
     *
     * This method emits an [onCurrentNodeDataUpdate] event only once after all annotations have been added.
     */
    fun addAnnotations(nodeToEdit: GameNode, annotations: Collection<Annotation>) {
        annotations.forEach { nodeToEdit.stateDelta.annotationsOnThisNode.add(it) }
        onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
    }

    /**
     * Invokes [removeAnnotation] using the current node.
     */
    fun removeAnnotationFromCurrentMove(x: Int, y: Int) {
        removeAnnotation(getCurrentMove(), x, y);
    }

    /**
     * Removes all annotation at the specified point from the current move. If one or more directional annotation has a
     * position here, it will be removed also.
     *
     * This method emits an [onNodeDataUpdate] event once after all annotations have been removed.
     */
    fun removeAnnotation(nodeToEdit: GameNode, x: Int, y: Int) {
        val annotationsToRemove = HashSet<Annotation>()

        for (annotation in getAnnotations(nodeToEdit)) {
            val onThisPoint = annotation.isOccupyingPosition(x, y)

            if (onThisPoint) {
                annotationsToRemove.add(annotation)
            }
        }

        annotationsToRemove.forEach { getCurrentMove().stateDelta.annotationsOnThisNode.remove(it) }
        onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
    }

    /**
     * Deletes an annotation from the current node.
     *
     * This method emits a [onNodeDataUpdate] event.
     */
    fun removeAnnotationFromCurrentMove(annotation: Annotation) {
        removeAnnotation(getCurrentMove(), annotation);
    }

    /**
     * Deletes an annotation from a specified node.
     *
     * This method emits a [onNodeDataUpdate] event.
     */
    fun removeAnnotation(nodeToEdit: GameNode, annotation: Annotation) {
        nodeToEdit.stateDelta.annotationsOnThisNode.remove(annotation)
        onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
    }

    /**
     * Deletes the given annotations from a specified node.
     *
     * This method emits a single [onNodeDataUpdate] event after all annotations have been removed.
     */
    fun removeAnnotations(nodeToEdit: GameNode, annotations: Collection<Annotation>) {
        annotations.forEach { nodeToEdit.stateDelta.annotationsOnThisNode.remove(it) }
        onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
    }

    /**
     * @return The current position the game is at.
     */
    fun getCurrentMove(): GameNode {
        return _currentMove
    }

    /**
     *
     * @return Set of all annotations on the current node.
     */
    fun getAnnotationsOnCurrentMove(): Set<Annotation> {
        return getAnnotations(getCurrentMove());
    }

    /**
     *
     * @return Set of all annotations on the specified node.
     */
    fun getAnnotations(node: GameNode): Set<Annotation> {
        return node.stateDelta.annotationsOnThisNode
    }

    /**
     *
     * @return The [StoneColor] for the stone that will be played on the next turn.
     */
    fun getNextTurnStoneColor(): StoneColor {
        return rules.getStoneColorForTurn(getNextMoveNumber())
    }

    /**
     *
     * @return Total number of intersections on this game board.
     */
    fun getIntersectionCount() = boardWidth * boardHeight

    /**
     * Returns a copy of the state hash history that led up to the current state.
     */
    fun getStateHashHistory(): List<Long> {
        return ArrayList(stateHashHistory)
    }

    fun getCurrentMoveStateDelta(): StateDelta {
        return getCurrentMove().stateDelta
    }

    /**
     * @return An integer representing the current move number, with zero being the root node.
     */
    fun getCurrentMoveNumber(): Int {
        return _currentMove.moveNumber
    }

    fun getNextMoveNumber(): Int {
        return getCurrentMoveNumber() + 1
    }

    /**
     * Adjusts the current move to the specified node.
     *
     * @throws IllegalArgumentException If the [node] is not part of the game tree.
     */
    fun setCurrentMove(node: GameNode) {
        _currentMove = node

        onCurrentNodeChange().fireEvent(NodeEvent(getCurrentMove()))
    }

    /**
     * @return The move preceding [getCurrentMove], or null if there is no previous move.
     */
    fun getPreviousMove(): GameNode? {
        return _currentMove.parent
    }

    /**
     * Java 8 friendly convenience method.
     *
     * @return The previous move if it exists, otherwise [Optional.empty]
     */
    fun getPreviousMoveOptional(): Optional<GameNode> {
        return Optional.ofNullable(getPreviousMove())
    }

    /**
     * Sets the current move to the previous move if it is not the root.
     * Otherwise, do nothing.
     *
     * @return The move preceding [getCurrentMove], or null if there is no previous move.
     */
    fun toPreviousMove(): GameNode? {

        return toPreviousMove(1)
    }

    /**
     * Go to the move that is n moves before the current node. If that move does not exist
     * because the top of the tree (root) is reached, the method fails gracefully by setting
     * the current position at the root node.
     *
     * @return The node this method arrived at (which will be the new current node).
     */
    fun toPreviousMove(steps: Int): GameNode {
        var newPosition = _currentMove

        for (i in 0 until steps) {
            newPosition.parent?.let {
                newPosition = it
            }
        }
        setCurrentMove(newPosition)

        return newPosition
    }

    /**
     *
     * @return The move following [getCurrentMove] that is on the main branch, or null if
     *         the current move is the last move in the variation.
     */
    fun getNextMoveInMainVariation(): GameNode? {
        return _currentMove.getNextMoveInMainBranch()
    }

    /**
     *
     * @return The move following [getCurrentMove] that is on the main branch, or [Optional.empty] if
     *         the current move is the last move in the variation.
     */
    fun getNextMoveInMainVariationOptional(): Optional<GameNode> {
        return _currentMove.getNextMoveInMainBranchOptional()
    }

    /**
     * @return All the possible variations following the current move. The first item in the
     *         result collection is the move in main variation.
     */
    fun getNextMoves(): Collection<GameNode> {
        return _currentMove.children
    }

    /**
     * Sets the current move to the next move if it is not the last move in this variation.
     * Otherwise, do nothing.
     *
     * @return The next move if it is available, otherwise null.
     */
    fun toNextMove(): GameNode {
        return toNextMove(1)
    }

    /**
     * Go to the move that is n moves after the current node. If that move does not exist because
     * the branch ended prematurely, the current move will be set to the last move in this branch.
     *
     * @return The node that this method arrived at (which will be the new current node).
     */
    fun toNextMove(steps: Int): GameNode {
        var newPosition = _currentMove

        for (i in 0 until steps) {
            if (newPosition.children.isNotEmpty()) {
                newPosition = newPosition.children[0]
            }
        }
        setCurrentMove(newPosition)

        return newPosition
    }

    /**
     * Appends the [node] to the game tree and sets the current move to the new [node].
     *
     * At this point the move is assumed to be legal and playable. Any rule validation should
     * take place before calling this method. In which case it may be preferable to define a
     * custom entry point to the move submission process.
     *
     * This method first emits an [onNodeAdd] event, followed by [onCurrentNodeChange].
     */
    fun submitMove(node: GameNode) {
        appendMove(node)
        setCurrentMove(node)
    }

    /**
     * Appends the node to the current node. If the current node has no children, the node will
     * become its main branch continuation. Otherwise, it becomes a variation. This method will not
     * update the current move to the new node.
     *
     * Use [submitMove] to append and set the current node.
     *
     * This method emits an [onNodeAdd] event.
     */
    fun appendMove(node: GameNode) {
        gameTree.appendNode(getCurrentMove(), node)
        onNodeAdd().fireEvent(NodeEvent(node))
    }

    /**
     * Removes the node and its subtree from the model. If the current move is part of the subtree deleted,
     * it will be reset to the parent of the deleted node.
     *
     * This method emits an [onNodeRemove] event.
     */
    fun removeNode(node: GameNode) {
        if (node.isRoot())
            throw IllegalArgumentException("Root node cannot be deleted")

        gameTree.removeNodeSubtree(node)
        onNodeRemove().fireEvent(NodeEvent(node))

        if (_currentMove.isContinuationOf(node)) {
            _currentMove = node.parent!! // Since root cannot be deleted, all other nodes should have a parent
        }
    }

    /**
     *
     * @return The root node of the game tree.
     */
    fun getRootNode(): GameNode {
        return gameTree.rootNode
    }

    // -- Observable property declarations

    /**
     * Emitter for current node change events. In other words, an event will be fired from this event hook
     * if a new game node becomes the current node.
     */
    fun onCurrentNodeChange(): NodeEventHook = currentNodeEventHook
    private val currentNodeEventHook = NodeEventHook()

    /**
     * Emitter for node addition events. This event is always emitted before [onCurrentNodeChange], clients
     * should not use [getCurrentMove] in the listener code block because the information is almost always
     * out of date.
     *
     * To retrieve the correct current node, subscribe to [onCurrentNodeChange] instead.
     */
    fun onNodeAdd(): NodeEventHook = nodeAdditionEventHook
    private val nodeAdditionEventHook = NodeEventHook()

    /**
     * Emitter for node deletion events.
     */
    fun onNodeRemove(): NodeEventHook = nodeDeletionEventHook
    private val nodeDeletionEventHook = NodeEventHook()

    /**
     * Emitter for current node data update events.
     *
     * This event does not represent a change in the current node, rather it is the change in the data on the current
     * node. To determine whether the current node itself has changed, subscribe to [onCurrentNodeChange] instead.
     */
    // Events in this hook doesn't need to be fired explicitly.
    fun onCurrentNodeDataUpdate(): NodeEventHook = currentNodeDataUpdateEventHook
    private val currentNodeDataUpdateEventHook = NodeEventHook()

    /**
     * Emitter for node data update events that may or may not be the current move.
     *
     * To subscribe to node data updates for only the current game move, use [onCurrentNodeDataUpdate].
     */
    fun onNodeDataUpdate(): NodeEventHook = nodeDataUpdateEventHook
    private val nodeDataUpdateEventHook = NodeEventHook()

    // This init block has to be done last because the fields are initialized in order.
    init {
        val currentNodeDataUpdateEventEmitter = object : EventListener<NodeEvent> {
            override fun onEvent(event: NodeEvent) {
                if (event.node == getCurrentMove()) {
                    onCurrentNodeDataUpdate().fireEvent(event)
                }
            }
        }

        onNodeDataUpdate().addListener(currentNodeDataUpdateEventEmitter)
    }
}