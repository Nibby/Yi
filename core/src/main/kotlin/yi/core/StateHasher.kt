package yi.core

/**
 * Transforms a [GoGameState] into a hash code such that the hash code is unique if and only if the game position is unique.
 * This is used to implement whole board positional repeat detection. Implementations must ensure that there is no
 * collision in hash code between unique game positions.
 */
interface StateHasher {

    /**
     * Generates a unique hash code which represents the given game state.
     */
    fun computeStateHash(state: GoGameState, boardWidth: Int, boardHeight: Int): Long

    /**
     * Generates a unique hash code based on the board state changes since last state hash.
     */
    fun computeUpdateHash(lastStateHash: Long, stoneUpdates: Set<StoneData>): Long

    /**
     * Generates the hash code that represents an empty board state.
     */
    fun computeEmptyPositionHash(boardWidth: Int, boardHeight: Int): Long {
        // Construct an empty board state and calculate its state hash
        val emptyStateData = HashSet<StoneData>()
        for (intersection in 0 until boardWidth * boardHeight) {
            val x = intersection % boardWidth
            val y = intersection / boardWidth
            emptyStateData.add(StoneData(x, y, GoStoneColor.NONE))
        }
        return computeUpdateHash(0, emptyStateData)
    }

}