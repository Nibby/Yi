package codes.nibby.yi.common

/**
 * The object representing a game instance whose mechanics can be modelled by a tree data type.
 * Each model consists of a [MoveTree], which is then comprised of a set of [MoveNode].
 */
abstract class BoardGameModel<NodeData> constructor(val rules: GameRulesHandler<NodeData>, tree: MoveTree<NodeData>) {

    var moveTree: MoveTree<NodeData>
        internal set

    var currentNode: MoveNode<NodeData>
        internal set

    init {
        this.moveTree = tree
        this.currentNode = moveTree.rootNode
    }

    /**
     * Instantiates a game document with an existing tree
     */
    constructor(rules: GameRulesHandler<NodeData>) : this(rules, MoveTree<NodeData>())

    /**
     * Adds a new node as a descendant of the [currentNode]
     */
    fun appendNode(newNode: MoveNode<NodeData>) {
        moveTree.appendNode(currentNode, newNode)
    }
}