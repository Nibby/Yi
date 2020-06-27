package codes.nibby.yi.weiqi

/**
 * Represents a point on the game board
 */
class Intersection constructor(val x: Int, val y: Int, val stoneColor: GoStoneColor?) {

    constructor(x: Int, y: Int) : this(x, y, null)

}