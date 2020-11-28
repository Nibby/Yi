package yi.core.go.rules

import yi.core.go.StoneColor

abstract class GameRulesHandler {

    /**
     * @param nextMoveNumber Move number with 0 being the first move.
     * @param handicap Number of handicap stones in this game.
     *
     * @return Color of the player expected to play at that given move number.
     */
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