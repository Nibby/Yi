package codes.nibby.yi.weiqi

import codes.nibby.yi.common.GameRulesHandler
import codes.nibby.yi.common.MoveNode

/**
 * Handles the rules around playing a move on an intersection in Go.
 */
internal object GoMoveHelper {

    fun validateAndGet(currentNode: MoveNode<GameStateDelta>, rules: GameRulesHandler<GameStateDelta>?, moveX: Int, moveY: Int)
            : MoveNode<GameStateDelta> {

        if (rules == null) {

        }

//        val delta = GameStateDelta()

        val newNode = MoveNode<GameStateDelta>()
        return MoveNode()
    }

}