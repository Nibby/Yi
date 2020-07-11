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
    }

}