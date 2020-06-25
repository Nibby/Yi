package codes.nibby.yi.common

/**
 * Represents one step in the [GoGame]. Each node stones the delta from its parent, as well as some
 * additional metadata such as move commentary. These data are represented as [NodeData] within the node.
 *
 * TODO: Complete the following section
 *       To obtain the view of the board state at this node position, use...
 *
 * @see NodeData
 */
class GameNode {

    /** Whether this node is the top level node in the game tree. */
    private var isRootNode: Boolean = false

    /** The game node that precedes this node. */
    private var parent: GameNode? = null
        private set

    /** The game nodes that succeed this node. */
    private val children: Set<GameNode> = LinkedHashSet()

    /** The main move represented by this node. */
    private var primaryMove: NodeStoneData? = null

    /** List of stones to be manually placed on the board. */
    private var helperStones: Set<NodeStoneData> = HashSet()

    /**
     * Appends the current node to the list of children of the parent, and updates the current node parent
     * to the candidate.
     *
     * @param parentCandidate Parent of this node.
     */
    fun setParent(parentCandidate: GameNode) {
        if (this.isRootNode)
            throw IllegalStateException("Cannot set parent node for root node!")
        if (this.parent != null)
            throw IllegalStateException("This node already has an active parent!")
        if (parentCandidate.children.contains(this))
            return // Safe to fail quietly because the desired outcome is already achieved

        this.parent = parentCandidate
        this.parent!!.addChildren(this)
    }

    /**
     * Assign one or more [GameNode] as descendants of this node.
     */
    fun addChildren(vararg childNodes: GameNode) {
        childNodes.forEach { node ->
            if (!children.contains(node))
                children + node
        }
    }

    /**
     * @return All data associated with this node, including the primary move.
     */
    fun getAllData(): Set<NodeData> {
        val allEntities = HashSet<NodeData>()

        primaryMove?.let { allEntities.add(it) }
        allEntities.addAll(helperStones)

        return allEntities
    }

    /**
     * Marks this node as the top level node of the game tree. This operation cannot be undone.
     */
    fun markAsRoot() {
        this.isRootNode = true
    }

    /**
     *
     */
    fun isRoot(): Boolean {
        return parent == null
    }
}