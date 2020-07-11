package codes.nibby.go

import java.lang.IllegalStateException

enum class GoStoneColor constructor(val index: Byte) {

    NONE(0) {
        override fun getOpponent(): GoStoneColor {
            throw IllegalStateException("GoStoneColor.NONE does not have an opponent color")
        }
    },

    BLACK(1) {
        override fun getOpponent(): GoStoneColor = WHITE
    },

    WHITE(2) {
        override fun getOpponent(): GoStoneColor = BLACK
    };

    abstract fun getOpponent(): GoStoneColor

}