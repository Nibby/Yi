package codes.nibby.yi.weiqi

import codes.nibby.yi.common.MoveNode

/**
 * Resolves a move node into a [GoGameState]
 */
object GoGameStateHelper {

    fun resolveGameState(game: GoGameModel, gameNode: MoveNode<GameStateDelta>): GoGameState {
        val positionState = GoGamePosition(game.boardWidth, game.boardHeight)

        var prisonersWhite = 0
        var prisonersBlack = 0

        // Build the board state by traversing the history and apply the delta from root up to gameNode
        gameNode.getPathToRoot().forEach { node ->
            node.data?.let { delta ->
                positionState.apply(delta)
                prisonersBlack += delta.captures.stream().filter { capture -> capture.stoneColor == GoStoneColor.BLACK }.count().toInt()
                prisonersWhite += delta.captures.stream().filter { capture -> capture.stoneColor == GoStoneColor.WHITE }.count().toInt()
            }

        }

        return GoGameState(game, positionState, gameNode, prisonersWhite, prisonersBlack)
    }

}
