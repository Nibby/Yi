package yi.core.go

class GoGamePosition(private val boardWidth: Int, boardHeight: Int) {

    init {
        if (boardWidth < 1 || boardHeight < 1)
            throw IllegalArgumentException("Invalid board dimensions: $boardWidth x $boardHeight")
    }

    val intersectionState = Array(boardWidth * boardHeight) { GoStoneColor.NONE }

    fun apply(update: GoGameStateUpdate) {
        // Apply primary move
        if (update.primaryMove != null) {
            val primaryMove = update.primaryMove.getPosition(boardWidth)
            intersectionState[primaryMove] = update.primaryMove.stoneColor
        }

        // Apply captures
        update.captures.forEach { intersectionState[it.getPosition(boardWidth)] = GoStoneColor.NONE }

        // Apply helper stone updates
        update.helperStoneUpdates.forEach { intersectionState[it.x + it.y * boardWidth] = it.stoneColor }
    }

    fun getStoneColorAt(x: Int, y:Int): GoStoneColor {
        return getStoneColorAt(x + y * boardWidth)
    }

    fun getStoneColorAt(position: Int): GoStoneColor {
        return intersectionState[position]
    }

    internal fun setStoneColorAt(x: Int, y: Int, stoneColor: GoStoneColor) {
        intersectionState[x + y * boardWidth] = stoneColor
    }
}