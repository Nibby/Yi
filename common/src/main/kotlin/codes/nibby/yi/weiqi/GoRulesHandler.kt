package codes.nibby.yi.weiqi

import codes.nibby.yi.common.BoardGameModel
import codes.nibby.yi.common.GameRulesHandler
import codes.nibby.yi.common.MoveNode
import codes.nibby.yi.common.payload.SubmitMoveParameters

abstract class GoRulesHandler : GameRulesHandler<GameStateDelta> {

    override fun playMove(gameModel: BoardGameModel<GameStateDelta>, moveParameters: SubmitMoveParameters): MoveNode<GameStateDelta> {
        assert(gameModel is GoGameModel) { "Unsupported board game model for this handler: " + gameModel.javaClass.name }

        return playMove(gameModel as GoGameModel, moveParameters)
    }

    fun playMove(game: GoGameModel, parameters: SubmitMoveParameters) : MoveNode<GameStateDelta> {
        val rulesHandler = if (parameters.ignoreRules) null else this
        val newNode = GoMoveHelper.validateAndGet(game.currentNode, rulesHandler, parameters.x, parameters.y)
        game.appendNode(newNode)

        return newNode
    }

    override fun isLegalMove(moveParameters: SubmitMoveParameters): Boolean {
        TODO("Not yet implemented")
    }

    fun getNextStoneColor(nextMoveNumber: Int): GoStoneColor {
        return if (nextMoveNumber % 2 == 1) GoStoneColor.BLACK else GoStoneColor.WHITE
    }

    abstract fun getKomi(): Float

}