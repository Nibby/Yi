package codes.nibby.yi.go

class GoGamePosition(val boardWidth: Int, val boardHeight: Int) {

    val intersectionState = Array(boardWidth * boardHeight) { GoStoneColor.NONE }

    fun apply(update: GameStateUpdate) {
        // Apply primary move
        if (update.primaryMove != null) {
            val primaryMove = update.primaryMove.getIndex(boardWidth)
            intersectionState[primaryMove] = update.primaryMove.stoneColor
        }

        // Apply captures
        update.captures.forEach { intersectionState[it.getIndex(boardWidth)] = GoStoneColor.NONE }

        // Apply helper stone updates
        update.helperStoneUpdates.forEach { intersectionState[it.x + it.y * boardWidth] = it.stoneColor }
    }

    fun getStoneColorAt(x: Int, y:Int): GoStoneColor {
        return getStoneColorAt(x + y * boardWidth)
    }

    fun getStoneColorAt(position: Int): GoStoneColor {
        return intersectionState[position]
    }
}