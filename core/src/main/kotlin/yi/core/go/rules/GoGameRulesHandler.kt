package yi.core.go.rules

import yi.core.go.StoneColor

abstract class GoGameRulesHandler {

    fun getStoneColorForTurn(nextMoveNumber: Int, handicap: Boolean): StoneColor {
        return if (handicap) {
            if (nextMoveNumber % 2 == 0) StoneColor.WHITE else StoneColor.BLACK
        } else {
            if (nextMoveNumber % 2 == 0) StoneColor.BLACK else StoneColor.WHITE
        }
    }

    abstract fun getDefaultKomi(): Float

    abstract fun allowSuicideMoves(): Boolean

    abstract fun getInternalName(): String

}