package yi.core.go

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * Represents a node on the [GameTree]. Each node stores a [StateDelta] which represents
 * the changes since the last game node.
 */
class GameNode constructor(val delta: StateDelta) {

    init {
        if (getType() == GameNodeType.ROOT) {
            markAsRoot()
        }
    }

    internal var markedAsRoot: Boolean = false

    /** Reference to the root of the tree this node belongs to. */
    internal var root: GameNode? = null

    internal var parent: GameNode? = null

    internal var children: ArrayList<GameNode> = ArrayList()

    var moveNumber: Int = 0
        internal set

    /**
     * Marks this node as the top-level ancestor node for the game tree.
     */
    private fun markAsRoot() {
        if (parent != null) {
            throw IllegalStateException("Node has an active parent")
        }

        root = this
        markedAsRoot = true
    }

    /**
     * Checks whether the current node follows from the supplied [node]. In other words,
     * tests if the current node is a descendant of (or belongs in the subtree of) the given node.
     *
     * @return true if the current node is a sequence continuation of the given node.
     */
    fun isContinuationOf(node: GameNode): Boolean {
        var currentNode = this

        val nodeDistance = node.moveNumber
        var currentDistance = currentNode.moveNumber

        while (!currentNode.isRoot()) {
            if (currentNode == node)
                return true

            currentNode = currentNode.parent!! // Not root, should always have a parent
            currentDistance--

            if (currentDistance < nodeDistance)
                return false
        }

        return false
    }

    /**
     *
     * @return true if this node is the top-level ancestor of the game tree.
     */
    fun isRoot(): Boolean = markedAsRoot

    /**
     *
     * @return true if this node is the last move in its branch.
     */
    fun isLastMove(): Boolean = parent != null && children.size == 0

    /**
     *
     * @return A chain of nodes that led up to the current node, starting from the root node.
     */
    fun getMoveHistory(): LinkedList<GameNode> {
        val path = LinkedList<GameNode>()
        var node: GameNode? = this

        while (node != null) {
            path.add(0, node)
            node = node.parent
        }

        return path
    }

    /**
     *
     * @return true if this position has more than 1 possible continuation.
     */
    fun hasAlternativeNextMoves(): Boolean {
        return getVariationsCount() > 1
    }

    /**
     *
     * @return All possible variations from this node.
     */
    fun getNextNodes(): List<GameNode> {
        return children
    }

    /**
     *
     * @return List of all immediate descendant nodes from this node that is not part of the
     *         main variation.
     */
    fun getNextNodesExcludingMainBranch(): List<GameNode> {
        val result = ArrayList<GameNode>()

        for (i in 1 until getVariationsCount()) {
            result.add(children[i])
        }

        return result
    }

    private fun getVariationsCount(): Int = children.size

    /**
     *
     * @return The next move in the main branch if it exists, otherwise null.
     */
    fun getNextNodeInMainBranch(): GameNode? {
        return if (getVariationsCount() > 0) children[0]
               else null
    }

    fun getType(): GameNodeType {
        return delta.type
    }

    fun getPrimaryMove(): Stone? {
        return delta.primaryMove
    }

    fun getCapturesCopy(): Collection<Stone> {
        return HashSet<Stone>(getCaptures())
    }

    internal fun getCaptures(): Collection<Stone> {
        return delta.captures
    }

    fun getStateHash(): Long {
        return delta.stateHash
    }

    fun getStoneEditsCopy(): Collection<Stone> {
        return HashSet<Stone>(getStoneEdits())
    }

    internal fun getStoneEdits(): Collection<Stone> {
        return delta.stoneEdits
    }

    internal fun addStoneEdit(stoneState: Stone) {
        delta.stoneEdits.add(stoneState)
    }

    internal fun addStoneEdits(stoneStates: Collection<Stone>) {
        stoneStates.forEach { stoneState -> addStoneEdit(stoneState) }
    }

    internal fun removeStoneEdit(stoneEdit: Stone) {
        delta.stoneEdits.remove(stoneEdit)
    }

    internal fun addAnnotation(annotation: Annotation) {
        delta.annotations.add(annotation)
    }

    internal fun addAnnotations(annotations: Collection<Annotation>) {
        annotations.forEach { annotation -> addAnnotation(annotation) }
    }

    internal fun removeAnnotation(annotation: Annotation) {
        delta.annotations.remove(annotation)
    }

    internal fun removeAnnotations(annotations: Collection<Annotation>) {
        annotations.forEach { annotation -> removeAnnotation(annotation) }
    }

    fun getAnnotationsCopy(): Collection<Annotation> {
        return HashSet<Annotation>(getAnnotationsOriginal())
    }

    fun getAnnotationsOriginal(): Collection<Annotation> {
        return delta.annotations
    }

    override fun toString(): String {
        return "Node ($moveNumber): $delta"
    }

    fun hasAnnotationAt(x: Int, y: Int): Boolean {
        return getAnnotationAt(x, y) != null;
    }

    fun getAnnotationAt(x: Int, y: Int): Annotation? {
        return getAnnotationsOriginal().firstOrNull { it.isOccupyingPosition(x, y) }
    }
}