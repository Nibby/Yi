package codes.nibby.yi.weiqi

class GoGamePosition(val boardWidth: Int, val boardHeight: Int) {

    enum class IllegalMoveReason {
        NONE,
        KO_RECAPTURE,
        BOARD_POSITION_REPEAT
    }

    val intersectionState = Array(boardWidth * boardHeight) { GoStoneColor.NONE }
    var illegalMovePosition = -1
    var illegalMoveReason = IllegalMoveReason.NONE

    fun apply(delta: GameStateDelta) {
        if (delta.primaryMove != null) {
            val primaryMove = delta.primaryMove.getIndex(boardWidth)
            intersectionState[primaryMove] = delta.primaryMove.stoneColor
        }

        delta.captures.forEach { intersectionState[it.getIndex(boardWidth)] = GoStoneColor.NONE }
        delta.helpersRemoved.forEach { intersectionState[it] = GoStoneColor.NONE }

        if (delta.illegalKoMove >= 0) {
            illegalMoveReason = IllegalMoveReason.KO_RECAPTURE
            illegalMovePosition = delta.illegalKoMove
        } else if (delta.illegalRepeatMove >= 0) {
            illegalMoveReason = IllegalMoveReason.BOARD_POSITION_REPEAT
            illegalMovePosition = delta.illegalRepeatMove
        }
    }
}