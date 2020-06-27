package codes.nibby.yi.weiqi

import codes.nibby.yi.common.GameRulesHandler

abstract class GoRulesHandler : GameRulesHandler<GameStateDelta> {

    fun getNextStoneColor(nextMoveNumber: Int): GoStoneColor {
        return if (nextMoveNumber % 2 == 1) GoStoneColor.BLACK else GoStoneColor.WHITE
    }

    abstract fun getKomi(): Float

}