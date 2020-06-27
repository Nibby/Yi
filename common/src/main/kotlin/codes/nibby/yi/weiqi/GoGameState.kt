package codes.nibby.yi.weiqi

import codes.nibby.yi.common.MoveNode
import codes.nibby.yi.common.GameState
import java.lang.IllegalArgumentException

/**
 * Represents the game position at a given [MoveNode]. Yi's implementation of game position is based on an incremental model.
 * This means that each [MoveNode] does not store the full game state at each position, rather, only the delta -- the change in
 * game state since the previous node. Therefore each node does not contain all the information to know the entire game state.
 *
 * To obtain the full game state at a given [MoveNode], use [GoGameStateHelper]
 *
 * @see GoGameStateHelper
 */
class GoGameState(private val gameModel: GoGameModel, private val boardPosition: Array<Intersection>, val prisonersWhite: Int, val prisonersBlack: Int) : GameState {

    fun getIntersection(x: Int, y:Int): Intersection {
        val boardWidth = gameModel.boardWidth
        val boardHeight = gameModel.boardHeight
        val positionIndex = x + y * boardWidth

        if (positionIndex < 0 || positionIndex >= boardWidth * boardHeight)
            throw IllegalArgumentException("Position out of bounds! Board size is $boardWidth x $boardHeight, accessing $x x $y")

        return boardPosition[positionIndex]
    }

}
