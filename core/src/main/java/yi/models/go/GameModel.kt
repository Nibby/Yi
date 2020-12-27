@file:Suppress("MemberVisibilityCanBePrivate") // Some methods may not be used, but is intended to be part of the public API

package yi.models.go

import yi.models.go.docformat.FileFormat
import yi.models.go.rules.GameRulesHandler
import java.lang.IllegalStateException
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList

/**
 * Representing one game of Go. The model tracks
 *
 * Unlike the [GameTree], methods in this model should be more domain-specific.
 *
 * //TODO: Explain the GameStateUpdate system
 * //TODO: Explain organisation internally, esp. how state is calculated and retrieved
 * //TODO: Explain how to submit moves
 */
class GameModel(val boardWidth: Int, val boardHeight: Int,
                val rules: GameRulesHandler, val stateHasher: GameStateHasher) {

    internal val gameTree = GameTree(stateHasher.computeEmptyPositionHash(boardWidth, boardHeight))
    private var _currentMove = gameTree.rootNode
        set(value) {
            if (!gameTree.isDescendant(value))
                throw IllegalArgumentException("Node does not belong to the model game tree")

            field = value
            internalCurrentNodeUpdate(value)
            currentNodeChangeEventHook.fireEvent(NodeEvent(value))
        }

    internal var playedMoveHistory: List<GameNode> = LinkedList()
    private var stateHashHistory: List<Long> = LinkedList()
    private var stateCache = WeakHashMap<Long, GameState>()

    var lastSavePath: Path? = null
    var lastSaveFormat: FileFormat? = null
    var isModified = false
    val info = GameModelInfo()

    init {
        if (boardWidth < 1 || boardHeight < 1) {
            throw IllegalArgumentException("Invalid board dimensions: $boardWidth x $boardHeight")
        }

        info.setKomi(rules.getDefaultKomi())
    }

    constructor(boardWidth: Int, boardHeight: Int, rulesHandler: GameRulesHandler)
            : this(boardWidth, boardHeight, rulesHandler, ZobristHasher(boardWidth, boardHeight))

    constructor(boardWidth: Int, boardHeight: Int, rulesStandard: StandardGameRules)
            : this(boardWidth, boardHeight, rulesStandard.getRulesHandler(), ZobristHasher(boardWidth, boardHeight))


    private fun internalCurrentNodeUpdate(currentNode: GameNode) {
        val nodeHistory = currentNode.getMoveHistory()
        this.playedMoveHistory = nodeHistory.filter {
            it.delta.type == GameNodeType.MOVE_PLAYED
                || it.delta.type == GameNodeType.PASS
        }
        if (nodeHistory.size > 1) {
            // Only count non-root and primary move updates for unique state
            val uniqueStateHistory = nodeHistory.subList(1, nodeHistory.size)
                    .filter { node -> node.getType() == GameNodeType.MOVE_PLAYED }
            this.stateHashHistory = uniqueStateHistory.map { node -> node.getStateHash() }
        } else {
            this.stateHashHistory = ArrayList()
        }
    }

    /**
     * Adjusts the root node of the game model. This is commonly used for loading a game
     * model from file, where the root node may contain additional metadata.
     *
     * This operation should only be performed once immediately after creating the game
     * model. If the current root node has descendants, the method will throw an
     * [IllegalStateException].
     */
    @Suppress("FunctionName") // Using underscore to denote non-standard operation
    internal fun _setRootNode(rootNode: GameNode) {
        if (gameTree.rootNode.getNextNodes().isNotEmpty()) {
            throw IllegalStateException("Cannot set root node after it has descendants. " +
                    "This operation should be performed before adding any additional nodes.")
        }

        val updateCurrentMove = _currentMove == gameTree.rootNode
        gameTree.rootNode = rootNode
        if (updateCurrentMove) {
            _currentMove = rootNode
        }
    }

    /**
     * Returns a handler to play a series of moves in succession. This is the recommended
     * approach when submitting multiple moves to the game model with the assumption that
     * each move must be submitted successfully.
     *
     * @see MoveSequence
     */
    fun beginMoveSequence(): MoveSequence {
        return MoveSequence(this)
    }

    /**
     * First checks if the move can be played at the current game position in compliance
     * with the game rules. If successful, appends a new node to the game tree.
     *
     * If the move is not compliant with the game rules, the method will fail silently
     * without submitting any new node to the game tree. To play a sequence of moves
     * ensuring each move is played correctly, use [beginMoveSequence] instead.
     *
     * If there is another move that is already played at the same co-ordinates following
     * the current move, then no new node will be submitted to the game tree. Instead,
     * the current position will be set to that move.
     *
     * @return The result of the request. See [MoveSubmitResult] for more information.
     */
    fun submitMove(x: Int, y: Int): MoveSubmitResult {
        return submitMove(getCurrentNode(), x, y)
    }

    fun submitMove(parent: GameNode, x: Int, y: Int): MoveSubmitResult {
        var identicalExistingMove: GameNode? = null

        for (child in parent.children) {
            child.getPrimaryMove()?.let {
                val moveX = it.x
                val moveY = it.y

                if (moveX == x && moveY == y) {
                    identicalExistingMove = child
                }
            }

            if (identicalExistingMove != null) {
                break
            }
        }

        val validationResult: MoveValidationResult
        val node: GameNode?
        val movePlayed: Boolean
        val reusedExistingNode: Boolean

        if (identicalExistingMove == null) {
            val validationAndNewNode = GameMoveSubmitter.createMoveNode(this,
                    parent, Stone(x, y, getNextTurnStoneColor()))

            validationResult = validationAndNewNode.first
            node = validationAndNewNode.second

            if (validationResult == MoveValidationResult.OK) {
                submitNode(node!!) // New node should not be null if validation result checks out
            }

            movePlayed = validationResult == MoveValidationResult.OK
            reusedExistingNode = false
            isModified = true
        } else {
            setCurrentNode(identicalExistingMove!!)

            validationResult = MoveValidationResult.OK
            node = identicalExistingMove
            movePlayed = true
            reusedExistingNode = true
        }

        return MoveSubmitResult(validationResult, node, movePlayed, reusedExistingNode)
    }

    /**
     * Forcefully submit a child move to the current move without validating it against
     * the game rules. This means that the move will be placed at the position regardless
     * if it is legal. Nearby captures will still be calculated.
     *
     * This method is often used by file importers to tolerate potentially corrupt or
     * incorrect game states when importing a game file. Normal gameplay should submit
     * moves using [submitMove] and its variants instead.
     *
     * Use this method with prudence, as it may result in an erroneous game state.
     */
    fun submitMoveWithoutValidation(x: Int, y: Int): MoveSubmitResult {
        return submitMoveWithoutValidation(_currentMove, x, y, getNextTurnStoneColor())
    }

    /**
     * Forcefully submit a child move to an existing node in the game tree without
     * validating it against the game rules. This means that the move will be placed
     * at the position regardless if it is legal. Nearby captures will still be calculated.
     *
     * This method is often used by file importers to tolerate potentially corrupt or
     * incorrect game states when importing a game file. Normal gameplay should submit
     * moves using [submitMove] and its variants instead.
     *
     * Use this method with prudence, as it may result in an erroneous game state.
     */
    fun submitMoveWithoutValidation(parent: GameNode, x: Int, y: Int,
                                    stoneColor: StoneColor): MoveSubmitResult {
        val validationAndNewNode = GameMoveSubmitter.createMoveNode(this,
                parent, Stone(x, y, stoneColor), ignoreRules = true)
        val newNode: GameNode? = validationAndNewNode.second
        submitNode(parent, newNode!!)

        return MoveSubmitResult(MoveValidationResult.OK, newNode, true)
    }

    /**
     * Play a pass for the next turn.
     */
    fun submitPass(): MoveSubmitResult {
        var existingContinuation: GameNode? = null

        for (child in getCurrentNode().children) {
            if (child.getType() == GameNodeType.PASS) {
                existingContinuation = child
            }
        }

        return if (existingContinuation != null) {
            setCurrentNode(existingContinuation)
            MoveSubmitResult(MoveValidationResult.OK, existingContinuation,
                    isPlayed = true, isReusingExistingNode = true)
        } else {
            val newNode = GameMoveSubmitter.createPassNode(getCurrentNode())
            submitNode(newNode)
            MoveSubmitResult(MoveValidationResult.OK, newNode,
                    isPlayed = true, isReusingExistingNode = false)
        }
    }

    /**
     * Submits a new node of type [GameNodeType.STONE_EDIT].
     *
     * The purpose of this node is to store all the manual edits made to the stones in the
     * current game position. Multiple edits to stones should be made under a single stone
     * edit node. In other words, for game records created within this game model, there
     * should never be multiple consecutive stone edit nodes.
     *
     * Consecutive stone edit nodes are permitted in the game tree because other styles
     * have been adopted by other programs / go servers.
     *
     * @return The newly created tree node for stone edits.
     */
    fun submitStoneEditNode(): GameNode {
        val node = GameMoveSubmitter.createStoneEditNode(getCurrentNode())
        submitNode(node)
        return node
    }

    /**
     * Equivalent to invoking [getGameState] at [getCurrentNode].
     *
     * @return The [GameState] at the current position.
     */
    fun getCurrentGameState(): GameState {
        return getGameState(getCurrentNode())
    }

    /**
     * The game state is dynamically calculated if it does not exist, otherwise it will be
     * cached for a period of time for performance.
     *
     * @return The [GameState] at a given node position.
     */
    fun getGameState(gameNode: GameNode): GameState {
        if (!gameTree.isDescendant(gameNode))
            throw IllegalArgumentException("Game node is not part of this move tree")

        if (gameNode.getType() == GameNodeType.MOVE_PLAYED) {
            this.stateCache[gameNode.getStateHash()]?.let {
                return it
            }
        }

        // Only perform state resolution if we don't have a cached position
        val positionState = GamePosition(boardWidth, boardHeight)

        var prisonersWhite = 0
        var prisonersBlack = 0

        val annotations: Collection<Annotation>

        // Build the board state by traversing the history and apply the delta from root up to gameNode
        val pathToRoot = gameNode.getMoveHistory()

        pathToRoot.forEach { node ->
            val captures = node.getCapturesThisTurn()

            positionState.apply(node.delta)
            prisonersWhite += captures.stream().filter { capture -> capture.color == StoneColor.BLACK }.count().toInt()
            prisonersBlack += captures.stream().filter { capture -> capture.color == StoneColor.WHITE }.count().toInt()
        }

        annotations = pathToRoot[pathToRoot.lastIndex].getAnnotations()

        val gameState = GameState(this, positionState, gameNode, prisonersWhite,
                prisonersBlack, annotations)
        this.stateCache[gameNode.getStateHash()] = gameState

        return gameState
    }

    /**
     * Set the move comments on the current node.
     *
     * This method emits [onNodeDataUpdate] and [onCurrentNodeDataUpdate]
     * events.
     */
    fun setCommentOnCurrentNode(comment: String) {
        setComment(getCurrentNode(), comment)
    }

    /**
     * Set the move comments on a node in this model.
     *
     * This method emits an [onNodeDataUpdate] event and optionally emits
     * an [onCurrentNodeDataUpdate] event if the node being edited is the
     * current node.
     */
    fun setComment(nodeToEdit: GameNode, comment: String) {
        nodeToEdit.setComments(comment)
        onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        isModified = true
    }

    /**
     * Adds one edited stone to the current node.
     *
     * This method emits an [onNodeDataUpdate] event.
     */
    fun addStoneEditToCurrentNode(stoneEdit: Stone) {
        addStoneEdit(getCurrentNode(), stoneEdit)
    }

    /**
     * Adds one edited stone to a specified node.
     *
     * This method emits an [onNodeDataUpdate] event.
     */
    fun addStoneEdit(nodeToEdit: GameNode, stoneEdit: Stone) {
        addStoneEdits(nodeToEdit, setOf(stoneEdit))
    }

    /**
     * Adds a collection of stone edits to the current node.
     *
     * This method emits only one [onNodeDataUpdate] event after all stones have been
     * added.
     */
    fun addStoneEdits(nodeToEdit: GameNode, stoneEdits: Collection<Stone>) {
        nodeToEdit.addStoneEdits(stoneEdits, stateHasher, boardWidth, boardHeight)
        onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        isModified = true
    }

    /**
     * Removes one edited stone from the specified node. If that stone edit does not exist,
     * does nothing.
     *
     * This method emits an [onNodeDataUpdate] event.
     */
    fun removeStoneEdit(nodeToEdit: GameNode, stoneEdit: Stone) {
        nodeToEdit.removeStoneEdit(stoneEdit, stateHasher, boardWidth, boardHeight)
        onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        isModified = true
    }

    /**
     * Adds one [Annotation] to the current move.
     *
     * This method emits an [onCurrentNodeDataUpdate] event.
     */
    fun addAnnotationToCurrentNode(annotation: Annotation) {
        addAnnotationsToCurrentNode(Collections.singleton(annotation))
    }

    /**
     * Calls [addAnnotations] using the current move.
     */
    fun addAnnotationsToCurrentNode(annotations: Collection<Annotation>) {
        addAnnotations(getCurrentNode(), annotations)
    }

    /**
     * Adds one or more [Annotation] to the current move.
     *
     * This method emits an [onCurrentNodeDataUpdate] event only once after all
     * annotations have been added.
     */
    fun addAnnotations(nodeToEdit: GameNode, annotations: Collection<Annotation>) {
        nodeToEdit.addAnnotations(annotations)
        onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        isModified = true
    }

    /**
     * Invokes [removeAnnotation] using the current node.
     */
    fun removeAnnotationFromCurrentNode(x: Int, y: Int) {
        removeAnnotation(getCurrentNode(), x, y)
    }

    /**
     * Removes all annotation at the specified point from the current move. If one or
     * more directional annotation has a position here, it will be removed also.
     *
     * This method emits an [onNodeDataUpdate] event once after all annotations
     * have been removed.
     */
    fun removeAnnotation(nodeToEdit: GameNode, x: Int, y: Int) {
        val annotationsToRemove = HashSet<Annotation>()

        val annotations = nodeToEdit.getAnnotations()
        for (annotation in annotations) {
            val onThisPoint = annotation.isOccupyingPosition(x, y)

            if (onThisPoint) {
                annotationsToRemove.add(annotation)
            }
        }

        getCurrentNode().removeAnnotations(annotationsToRemove)
        onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        isModified = true
    }

    /**
     * Deletes an annotation from the current node.
     *
     * This method emits a [onNodeDataUpdate] event.
     */
    fun removeAnnotationFromCurrentNode(annotation: Annotation) {
        removeAnnotation(getCurrentNode(), annotation)
    }

    /**
     * Deletes an annotation from a specified node.
     *
     * This method emits a [onNodeDataUpdate] event.
     */
    fun removeAnnotation(nodeToEdit: GameNode, annotation: Annotation) {
        nodeToEdit.removeAnnotation(annotation)
        onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        isModified = true
    }

    /**
     * Deletes the given annotations from a specified node.
     *
     * This method emits a single [onNodeDataUpdate] event after all annotations have
     * been removed.
     */
    fun removeAnnotations(nodeToEdit: GameNode, annotations: Collection<Annotation>) {
        nodeToEdit.removeAnnotations(annotations)
        onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        isModified = true
    }

    /**
     * @return The current position the game is at.
     */
    fun getCurrentNode(): GameNode {
        return _currentMove
    }

    /**
     *
     * @return Set of all annotations on the current node.
     */
    fun getAnnotationsOnCurrentNode(): Collection<Annotation> {
        return getAnnotations(getCurrentNode())
    }

    /**
     *
     * @return Set of all annotations on the specified node.
     */
    fun getAnnotations(node: GameNode): Collection<Annotation> {
        return node.getAnnotations()
    }

    /**
     *
     * @return The [StoneColor] for the stone that will be played on the next turn.
     */
    fun getNextTurnStoneColor(): StoneColor {
        return rules.getStoneColorForTurn(playedMoveHistory.size, info.getHandicapCount() > 0)
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

    /**
     * The move number includes stone edit nodes as well as played moves, pass and
     * resignations.
     *
     * @return An integer representing the current move number, with zero being the root
     * node.
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
    fun setCurrentNode(node: GameNode) {
        _currentMove = node

        onCurrentNodeChange().fireEvent(NodeEvent(getCurrentNode()))
    }

    /**
     * @return The move preceding [getCurrentNode], or null if there is no previous move.
     */
    fun getPreviousNode(): GameNode? {
        return _currentMove.parent
    }

    /**
     * Sets the current move to the previous move if it is not the root.
     * Otherwise, do nothing.
     *
     * @return The move preceding [getCurrentNode], or null if there is no previous move.
     */
    fun toPreviousNode(): GameNode? {

        return toPreviousNode(1)
    }

    /**
     * Go to the move that is n moves before the current node. If that move does not exist
     * because the top of the tree (root) is reached, the method fails gracefully by
     * setting the current position at the root node.
     *
     * @return The node this method arrived at (which will be the new current node).
     */
    fun toPreviousNode(steps: Int): GameNode {
        var newPosition = _currentMove

        for (i in 0 until steps) {
            newPosition.parent?.let {
                newPosition = it
            }
        }
        setCurrentNode(newPosition)

        return newPosition
    }

    /**
     * Sets the current move to the next move if it is not the last move in this variation.
     * Otherwise, do nothing.
     *
     * @return The next move if it is available, otherwise null.
     */
    fun toNextNode(): GameNode {
        return toNextNode(1)
    }

    /**
     * Go to the move that is n moves after the current node. If that move does not exist
     * because the branch ended prematurely, the current move will be set to the last move
     * in this branch.
     *
     * @return The node that this method arrived at (which will be the new current node).
     */
    fun toNextNode(steps: Int): GameNode {
        var newPosition = _currentMove

        for (i in 0 until steps) {
            if (newPosition.children.isNotEmpty()) {
                newPosition = newPosition.children[0]
            }
        }
        setCurrentNode(newPosition)

        return newPosition
    }

    /**
     * Appends the node to the game tree and sets the current move to the new node.
     *
     * At this point the move is assumed to be legal and playable. Any rule validation should
     * take place before calling this method. In which case it may be preferable to define a
     * custom entry point to the move submission process.
     *
     * This method first emits an [onNodeAdd] event, followed by [onCurrentNodeChange].
     */
    fun submitNode(node: GameNode) {
        submitNode(getCurrentNode(), node)
    }

    /**
     * Appends the child to the parent node and set the child as the current move. The
     * parent node must already exist in the game tree.
     *
     * At this point the move is assumed to be legal and playable. Any rule validation
     * should take place before calling this method. In which case it may be preferable
     * to define a custom entry point to the move submission process.
     *
     * This method first emits an [onNodeAdd] event, followed by [onCurrentNodeChange].
     */
    fun submitNode(parent: GameNode, child: GameNode) {
        appendNode(parent, child)
        setCurrentNode(child)
        isModified = true
    }

    /**
     * Appends the node to the current node. If the current node has no children, the node
     * will become its main branch continuation. Otherwise, it becomes a variation. This
     * method will not update the current move to the new node.
     *
     * Use [submitMove] to append and set the current node.
     *
     * This method emits an [onNodeAdd] event.
     */
    fun appendNode(node: GameNode) {
        appendNode(getCurrentNode(), node)
    }

    /**
     * Appends a child node to an existing node (parent) on the tree.
     *
     * If the parent node has no children, the child move will be come its main branch
     * continuation. Otherwise, it becomes a variation. This method will not update
     * the current move to the newly appended node.
     *
     * Use [submitMove] to append and set the current node.
     *
     * This method emits an [onNodeAdd] event.
     */
    fun appendNode(parent: GameNode, child: GameNode) {
        gameTree.appendNode(parent, child)
        onNodeAdd().fireEvent(NodeEvent(child))
        isModified = true
    }

    /**
     * Removes the node and its subtree from the model. If the current move is part of the
     * subtree deleted, it will be reset to the parent of the deleted node.
     *
     * If the node to be deleted is the root, only its children will be removed and the
     * current move will be set to root.
     *
     * This method emits an [onNodeRemove] event.
     */
    fun removeNodeSubtree(node: GameNode) {
        // Do it here because the node lineage will be destroyed after removing node subtree.
        // and we want to fire the current node change event after node removal event.
        val readjustCurrentNode = _currentMove.isContinuationOf(node)

        // Since root cannot be deleted, there will always be a parent for the node to be removed.
        val nodeToAdjustTo = if (readjustCurrentNode) {
            if (node.isRoot()) node
            else node.parent!!
        } else null

        gameTree.removeNodeSubtree(node)

        // TODO: If removing towards the start of a very large tree, the deletion is also
        //       fired for each node which may lead to performance issues...
        onNodeRemove().fireEvent(NodeEvent(node))

        if (readjustCurrentNode) {
            _currentMove = nodeToAdjustTo!!
        }

        isModified = true
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
     * Emitter for current node change events. In other words, an event will be fired from
     * this event hook if a new game node becomes the current node.
     */
    fun onCurrentNodeChange(): NodeEventHook = currentNodeChangeEventHook
    private val currentNodeChangeEventHook = NodeEventHook()

    /**
     * Emitter for node addition events. This event is always emitted before
     * [onCurrentNodeChange], clients should not use [getCurrentNode] in the listener
     * code block because the information is almost always out of date.
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
     * This event does not represent a change in the current node, rather it is the change
     * in the data on the current node. To determine whether the current node itself has
     * changed, subscribe to [onCurrentNodeChange] instead.
     */
    // Events in this hook doesn't need to be fired explicitly.
    fun onCurrentNodeDataUpdate(): NodeEventHook = currentNodeDataUpdateEventHook
    private val currentNodeDataUpdateEventHook = NodeEventHook()

    /**
     * Emitter for node data update events that may or may not be the current move.
     *
     * To subscribe to node data updates for only the current game move, use
     * [onCurrentNodeDataUpdate].
     */
    fun onNodeDataUpdate(): NodeEventHook = nodeDataUpdateEventHook
    private val nodeDataUpdateEventHook = NodeEventHook()

    /**
     * Destroys the game model by removing all data from the game tree and clearing all
     * event hook listeners.
     */
    fun dispose() {
        onCurrentNodeChange().removeAllListeners()
        onNodeAdd().removeAllListeners()
        onNodeRemove().removeAllListeners()
        onCurrentNodeDataUpdate().removeAllListeners()
        onNodeDataUpdate().removeAllListeners()

        removeNodeSubtree(getRootNode())
        info.dispose()
    }

    // This init block has to be done last because the fields are initialized in order.
    init {
        val currentNodeDataUpdateEventEmitter = object : EventListener<NodeEvent> {
            override fun onEvent(event: NodeEvent) {
                if (event.node == getCurrentNode()) {
                    onCurrentNodeDataUpdate().fireEvent(event)
                }
            }
        }

        onNodeDataUpdate().addListener(currentNodeDataUpdateEventEmitter)
    }
}