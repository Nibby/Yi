package codes.nibby.yi.go

/**
 * Represents a point on the game board
 */
class StoneData constructor(val x: Int, val y: Int, val stoneColor: GoStoneColor) {

    fun getIndex(boardWidth: Int) = x + y * boardWidth

}