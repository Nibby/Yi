package yi.core.go

import java.util.*
import kotlin.collections.ArrayList

/*
 * Each node stores a [StateDelta] which represents the changes since the last game node.
 */

/**
 * Represents a node on the [GameTree]. Data on the node can be manipulated using setter
 * methods in [GameModel]. Getters in this class always return an immutable collection.
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

    var parent: GameNode? = null
        internal set

    internal var children: ArrayList<GameNode> = ArrayList()

    var moveNumber: Int = 0
        internal set

    private var cachedMoveHistory: LinkedList<GameNode>? = null

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
        return getMoveHistory().contains(node)
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
    fun isLastMoveInThisVariation(): Boolean = children.size == 0

    /**
     *
     * @return A chain of nodes that led up to the current node, starting from the root node.
     */
    fun getMoveHistory(): List<GameNode> {
        cachedMoveHistory?.let {
            return Collections.unmodifiableList(it)
        }

        val path = LinkedList<GameNode>()
        var node: GameNode? = this

        while (node != null) {
            path.add(0, node)
            node = node.parent
        }
        cachedMoveHistory = path
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

    fun getChildOrder(node: GameNode): Int {
        return children.indexOf(node)
    }

    private fun getVariationsCount(): Int = children.size

    /**
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

    fun getCaptures(): Collection<Stone> {
        return Collections.unmodifiableSet(delta.captures)
    }

    fun getStateHash(): Long {
        return delta.stateHash
    }

    fun getStoneEdits(): Collection<Stone> {
        return Collections.unmodifiableSet(delta.stoneEdits)
    }

    internal fun addStoneEdit(stoneState: Stone, stateHasher: GameStateHasher,
                              boardWidth: Int, boardHeight: Int) {
        delta.stoneEdits.add(stoneState)
        recomputeStateHash(stateHasher, boardWidth, boardHeight)
    }

    internal fun addStoneEdits(stoneStates: Collection<Stone>, stateHasher: GameStateHasher,
                               boardWidth: Int, boardHeight: Int) {
        stoneStates.forEach { stoneState -> delta.stoneEdits.add(stoneState) }
        recomputeStateHash(stateHasher, boardWidth, boardHeight)
    }

    internal fun removeStoneEdit(stoneEdit: Stone, stateHasher: GameStateHasher,
                                 boardWidth: Int, boardHeight: Int) {
        delta.stoneEdits.remove(stoneEdit)
        recomputeStateHash(stateHasher, boardWidth, boardHeight)
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

    fun getAnnotations(): Collection<Annotation> {
        return Collections.unmodifiableSet(delta.annotations)
    }

    override fun toString(): String {
        return "Node ($moveNumber): $delta"
    }

    fun hasAnnotationAt(x: Int, y: Int): Boolean {
        return getAnnotationAt(x, y) != null
    }

    fun getAnnotationAt(x: Int, y: Int): Annotation? {
        return getAnnotations().firstOrNull { it.isOccupyingPosition(x, y) }
    }

    /**
     * @return stone edit on this node at the given position if it exists.
     */
    fun getStoneEditAt(x: Int, y: Int): Stone? {
        val edit = delta.stoneEdits.firstOrNull { it.x == x && it.y == y }

        if (edit != null) {
            return edit
        }

        return null
    }

    /**
     *
     * @return Comments associated with this move, or empty string if none.
     */
    fun getComments(): String {
        return delta.comments
    }

    /**
     * Stores a series of key value pairs that are not semantically meaningful to this
     * application, but is present in the node data (usually from an externally loaded file).
     */
    fun putMetadata(key: String, value: String) {
        putMetadata(key, listOf(value))
    }

    /**
     * Stores a series of key value pairs that are not semantically meaningful to this
     * application, but is present in the node data (usually from an externally loaded file).
     */
    fun putMetadata(key: String, value: List<String>) {
        delta.metadata[key] = value
    }

    /**
     * Stores a series of key value pairs that are not semantically meaningful to this
     * application, but is present in the node data (usually from an externally loaded file).
     * This way the data can be persisted when the user saves their document from this
     * application.
     */
    fun putMetadata(data: Map<String, List<String>>) {
        for (key in data.keys) {
            putMetadata(key, data.getValue(key))
        }
    }

    /**
     * This method is used for cases where exactly one metadata value is expected
     * for the given key. For scenarios where multiple values may be present, use
     * [getMetadataMultiValue] instead.
     *
     * This method will generate an [IllegalArgumentException] if more than one value
     * is present for the key.
     *
     * @return The one and only metadata value associated with this key.
     */
    fun getMetadataSingleValue(key: String): String? {
        val values = getMetadataMultiValue(key)

        if (values.size > 1) {
            throw IllegalArgumentException("More than 1 value present for key \"$key\"")
        }

        return if (values.isEmpty()) null else values[0]
    }

    /**
     * @return All the values associated with this metadata key on this node.
     *         If no such property exists, returns an empty list.
     */
    fun getMetadataMultiValue(key: String): List<String> {
        return delta.metadata.getOrDefault(key, listOf())
    }

    private fun recomputeStateHash(stateHasher: GameStateHasher, boardWidth: Int, boardHeight: Int) {
        val lastHash: Long =
                if (parent != null) parent!!.getStateHash()
                else stateHasher.computeEmptyPositionHash(boardWidth, boardHeight)

        val newHash = stateHasher.computeUpdateHash(lastHash, delta.stoneEdits)
        delta.stateHash = newHash
    }
}