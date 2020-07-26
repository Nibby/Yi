package yi.core.go.rules

import yi.core.go.StoneColor

abstract class GoGameRulesHandler {

    fun getStoneColorForTurn(nextMoveNumber: Int): StoneColor {
        return if (nextMoveNumber % 2 == 1) StoneColor.BLACK else StoneColor.WHITE
    }

    abstract fun getKomi(): Float

    abstract fun allowSuicideMoves(): Boolean

}