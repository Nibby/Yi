package codes.nibby.yi.weiqi

/**
 * Transforms a [GoGameState] into a hash code such that the code is unique if and only if the game position is unique.
 */
interface StateHasher {

    /**
     * Generates a unique hash code which represents the game state.
     */
    fun calculateStateHash(state: GoGameState): Long

}