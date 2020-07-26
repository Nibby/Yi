package yi.core.go

import java.lang.IllegalStateException

enum class StoneColor constructor(val index: Byte) {

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