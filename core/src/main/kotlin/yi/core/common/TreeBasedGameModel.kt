package yi.core.common

/**
 * An abstract data structure to represent a game whose states can be modelled by
 * a tree. The model is designed to be state-based -- that there is always a
 * current 'position' at any point in time.
 *
 * Unlike the [GameTree], methods in this model should be more domain-specific.
 */
interface TreeBasedGameModel<NodeData> {

    /**
     * @return The current position the game is at.
     */
    fun getCurrentMove(): GameNode<NodeData>

    /**
     * @return An integer starting at zero for the root node.
     */
    fun getCurrentMoveNumber(): Int

    /**
     * Adjusts the current move to the specified node.
     *
     * @throws IllegalArgumentException If the [node] is not part of the game tree.
     */
    fun setCurrentMove(node: GameNode<NodeData>)

    /**
     * @return The move preceding [getCurrentMove], or null if there is no previous move.
     */
    fun getPreviousMove(): GameNode<NodeData>?

    /**
     * Sets the current move to the previous move if it is not the root.
     * Otherwise, do nothing.
     *
     * @return The move preceding [getCurrentMove], or null if there is no previous move.
     */
    fun toPreviousMove(): GameNode<NodeData>?

    /**
     * @return The move following [getCurrentMove], or null if the current move is the last
     *         move in the variation.
     */
    fun getNextMove(): GameNode<NodeData>?

    /**
     * @return All the possible variations following the current move. The first item in the
     *         result collection is the move in main variation.
     */
    fun getNextMoves(): Collection<GameNode<NodeData>>

    /**
     * Sets the current move to the next move if it is not the last move in this variation.
     * Otherwise, do nothing.
     *
     * @return The next move if it is available, otherwise null.
     */
    fun toNextMove(): GameNode<NodeData>?

    /**
     * Appends the [node] to the game tree and sets the current move to the new [node].
     *
     * At this point the move is assumed to be legal and playable. Any rule validation should
     * take place before calling this method. In which case it may be preferable to define a
     * custom entry point to the move submission process.
     */
    fun submitMove(node: GameNode<NodeData>)

}