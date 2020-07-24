package yi.core.common

abstract class AbstractTreeBasedGameModel<Data>(rootNodeData: Data) : TreeBasedGameModel<Data> {

    internal val gameTree = GameTree<Data>()
    private var _currentMove = gameTree.rootNode
        set(value) {
            if (!gameTree.isDescendant(value))
                throw IllegalArgumentException("Node does not belong to the model game tree")

            field = value
            onCurrentNodeUpdate(field)
        }

    init {
        gameTree.rootNode.data = rootNodeData
    }

    protected abstract fun onCurrentNodeUpdate(currentNode: GameNode<Data>)

    override fun getCurrentMove(): GameNode<Data> {
        return _currentMove
    }

    fun getCurrentMoveData(): Data? {
        return getCurrentMove().data
    }

    override fun getCurrentMoveNumber(): Int {
        return _currentMove.getDistanceToRoot()
    }

    fun getNextMoveNumber(): Int {
        return getCurrentMoveNumber() + 1
    }

    override fun setCurrentMove(node: GameNode<Data>) {
        _currentMove = node

        currentNodeEventHook.fireEvent(NodeEvent(_currentMove))
    }

    override fun getPreviousMove(): GameNode<Data>? {
        return _currentMove.parent
    }

    override fun toPreviousMove(): GameNode<Data>? {
        val parent = _currentMove.parent
        parent?.let { setCurrentMove(it) }

        return parent
    }

    override fun getNextMove(): GameNode<Data>? {
        return if (_currentMove.children.isNotEmpty()) _currentMove.children[0]
               else null
    }

    override fun getNextMoves(): Collection<GameNode<Data>> {
        return _currentMove.children
    }

    override fun toNextMove(): GameNode<Data>? {
        val nextMove = getNextMove()
        nextMove?.let { setCurrentMove(it) }

        return nextMove
    }

    override fun submitMove(node: GameNode<Data>) {
        appendMove(node)
        setCurrentMove(node)
    }

    fun appendMove(node: GameNode<Data>) {
        gameTree.appendNode(getCurrentMove(), node)
        nodeAdditionEventHook.fireEvent(NodeEvent(node))
    }

    fun deleteMove(node: GameNode<Data>) {
        if (node.isRoot())
            throw IllegalArgumentException("Root node cannot be deleted")

        gameTree.removeNodeSubtree(node)
        nodeDeletionEventHook.fireEvent(NodeEvent(node))

        if (_currentMove.isDescendantOf(node)) {
            _currentMove = node.parent!! // Since root cannot be deleted, all other nodes should have a parent
        }
    }

    // -- Observable property declarations
    private val currentNodeEventHook = NodeEventHook<Data>()
    fun onCurrentNodeUpdate(): NodeEventHook<Data> = currentNodeEventHook

    private val nodeAdditionEventHook = NodeEventHook<Data>()
    fun onNodeAdd(): NodeEventHook<Data> = nodeAdditionEventHook

    private val nodeDeletionEventHook = NodeEventHook<Data>()
    fun onNodeDelete(): NodeEventHook<Data> = nodeDeletionEventHook
}