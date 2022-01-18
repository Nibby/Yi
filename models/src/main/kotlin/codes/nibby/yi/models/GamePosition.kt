package codes.nibby.yi.models

/**
 * Describes the board position at a particular [GameState].
 */
class GamePosition(private val boardWidth: Int, boardHeight: Int) {

    init {
        if (boardWidth < 1 || boardHeight < 1)
            throw IllegalArgumentException("Invalid board dimensions: $boardWidth x $boardHeight")
    }

    val intersectionState = Array(boardWidth * boardHeight) { StoneColor.NONE }

    /**
     * Updates the current position based on the provided delta.
     */
    internal fun apply(update: StateDelta) {
        // Apply captures
        update.captures.forEach { intersectionState[it.getPosition(boardWidth)] =
            StoneColor.NONE
        }

        // Apply stone updates
        update.stoneEdits.forEach { intersectionState[it.x + it.y * boardWidth] = it.color }
    }

    /**
     * @param x x intersection position.
     * @param y y intersection position.
     * @return Color of the stone at the given co-ordinate. If no stone exists there,
     * returns [StoneColor.NONE].
     */
    fun getStoneColorAt(x: Int, y:Int): StoneColor {
        return getStoneColorAt(x + y * boardWidth)
    }

    /**
     * This is the one variable form of the stone position, calculated using
     * y * boardWidth + x.
     *
     * @return Color of the stone at the given co-ordinate. If no stone exists there,
     * returns [StoneColor.NONE].
     */
    fun getStoneColorAt(position: Int): StoneColor {
        return intersectionState[position]
    }
}