package codes.nibby.yi.models

import java.util.*

// Fields in this class must be immutable.

/**
 * A tuple that describes the state of a position.
 */
class Stone constructor(val x: Int, val y: Int, val color: StoneColor) {

    /**
     * @return index-based position representation of this stone.
     */
    fun getPosition(boardWidth: Int) = x + y * boardWidth

    override fun equals(other: Any?): Boolean {
        if (other is Stone) {
            return this.x == other.x && this.y == other.y && this.color == other.color
        }

        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(x, y, color)
    }

    override fun toString(): String {
        return "($x, $y): $color"
    }
}