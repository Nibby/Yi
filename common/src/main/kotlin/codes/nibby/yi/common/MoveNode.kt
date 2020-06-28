package codes.nibby.yi.common

import java.util.*
import kotlin.collections.ArrayList

/**
 * Represents a node on the [MoveTree]. Each node may optionally store an instance of the current game state.
 *
 * @param NodeData The current state information represented by this node.
 */
open class MoveNode<NodeData> constructor() {

    internal var markedAsRoot: Boolean = false

    /** Reference to the root of the tree this node belongs to. */
    var root: MoveNode<NodeData>? = null
        internal set

    var parent: MoveNode<NodeData>? = null
        internal set

    var children: ArrayList<MoveNode<NodeData>> = ArrayList()
        internal set

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

    fun getPathToRoot(): LinkedList<MoveNode<NodeData>> {
        val path = LinkedList<MoveNode<NodeData>>()
        var node: MoveNode<NodeData>? = this

        while (node != null) {
            path.add(0, node)
            node = node.parent
        }

        return path
    }

    fun getTreeRoot(): MoveNode<NodeData>? = root

    override fun toString(): String {
        return "Node (" + getDistanceToRoot() + "): " + data.toString()
    }
}