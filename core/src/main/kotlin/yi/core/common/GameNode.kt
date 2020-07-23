package yi.core.common

import java.util.*
import kotlin.collections.ArrayList

/**
 * Represents a node on the [GameTree]. Each node may optionally store an instance of the current game state.
 *
 * @param NodeData The current state information represented by this node.
 */
open class GameNode<NodeData> constructor() {

    internal var markedAsRoot: Boolean = false

    /** Reference to the root of the tree this node belongs to. */
    var root: GameNode<NodeData>? = null
        internal set

    var parent: GameNode<NodeData>? = null
        internal set

    var children: ArrayList<GameNode<NodeData>> = ArrayList()
        internal set
        get() {
            return ArrayList(field)
        }

    var data: NodeData? = null
        internal set

    var position: Int = 0

    constructor(data: NodeData) : this() {
        this.data = data;
    }

    fun hasData(): Boolean = data != null

    fun markAsRoot() {
        if (parent != null)
            throw IllegalStateException("Node has an active parent")

        root = this
        markedAsRoot = true
    }

    fun isRoot(): Boolean = parent == null || markedAsRoot

    fun isLeaf(): Boolean = parent != null && children.size == 0

    fun getDistanceToRoot(): Int {
        return position
    }

    fun getPathToRoot(): LinkedList<GameNode<NodeData>> {
        val path = LinkedList<GameNode<NodeData>>()
        var node: GameNode<NodeData>? = this

        while (node != null) {
            path.add(0, node)
            node = node.parent
        }

        return path
    }

    /**
     * @return true if this position has more than 1 possible continuation.
     */
    fun hasOtherVariations(): Boolean {
        return getChildrenExcludingMainVariation().isNotEmpty()
    }

    /**
     * @return All child nodes except the node that is part of the main variation.
     */
    fun getChildrenExcludingMainVariation(): List<GameNode<NodeData>> {
        val allChildren = children

        if (allChildren.size <= 1)
            return ArrayList()

        allChildren.removeAt(0)
        return allChildren
    }

    override fun toString(): String {
        return "Node (" + getDistanceToRoot() + "): " + data.toString()
    }
}