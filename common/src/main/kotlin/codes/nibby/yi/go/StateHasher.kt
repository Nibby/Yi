package codes.nibby.yi.go

/**
 * Transforms a [GoGameState] into a hash code such that the code is unique if and only if the game position is unique.
 */
interface StateHasher {

    /**
     * Generates a unique hash code which represents the game state.
     */
    fun calculateStateHash(state: GoGameState, boardWidth: Int, boardHeight: Int): Long

    fun calculateUpdateHash(currentStateHash: Long, stoneUpdates: Set<StoneData>): Long

    fun getEmptyStateHash(boardWidth: Int, boardHeight: Int): Long {
        // Construct an empty board state and calculate its state hash
        val emptyStateData = HashSet<StoneData>()
        for (intersection in 0 until boardWidth * boardHeight) {
            val x = intersection % boardWidth
            val y = intersection / boardWidth
            emptyStateData.add(StoneData(x, y, GoStoneColor.NONE))
        }
        return calculateUpdateHash(0, emptyStateData)
    }

}