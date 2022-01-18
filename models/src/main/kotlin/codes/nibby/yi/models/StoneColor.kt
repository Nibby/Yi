package codes.nibby.yi.models

/**
 * Describes the color of a stone. Only conventional colors are supported.
 */
enum class StoneColor constructor(val index: Byte) {

    /**
     * Usually used to represent no stone is present.
     */
    NONE(0) {
        override fun getOpponent(): StoneColor {
            throw IllegalStateException("GoStoneColor.NONE does not have an opponent color")
        }
    },

    BLACK(1) {
        override fun getOpponent(): StoneColor = WHITE
    },

    WHITE(2) {
        override fun getOpponent(): StoneColor = BLACK
    };

    abstract fun getOpponent(): StoneColor

}