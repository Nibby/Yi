package yi.core.go

class GamePosition(private val boardWidth: Int, boardHeight: Int) {

    init {
        if (boardWidth < 1 || boardHeight < 1)
            throw IllegalArgumentException("Invalid board dimensions: $boardWidth x $boardHeight")
    }

    val intersectionState = Array(boardWidth * boardHeight) { StoneColor.NONE }

    fun apply(update: StateDelta) {
        // Apply primary move
        if (update.primaryMove != null) {
            val primaryMove = update.primaryMove.getPosition(boardWidth)
            intersectionState[primaryMove] = update.primaryMove.color
        }

        // Apply captures
        update.captures.forEach { intersectionState[it.getPosition(boardWidth)] = StoneColor.NONE }

        // Apply helper stone updates
        update.stoneEdits.forEach { intersectionState[it.x + it.y * boardWidth] = it.color }
    }

    fun getStoneColorAt(x: Int, y:Int): StoneColor {
        return getStoneColorAt(x + y * boardWidth)
    }

    fun getStoneColorAt(position: Int): StoneColor {
        return intersectionState[position]
    }
}