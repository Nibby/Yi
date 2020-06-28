package codes.nibby.yi.go

abstract class GoGameRulesHandler {

    fun getStoneColorForTurn(nextMoveNumber: Int): GoStoneColor {
        return if (nextMoveNumber % 2 == 1) GoStoneColor.BLACK else GoStoneColor.WHITE
    }

    abstract fun getKomi(): Float

    abstract fun allowSuicideMoves(): Boolean

}