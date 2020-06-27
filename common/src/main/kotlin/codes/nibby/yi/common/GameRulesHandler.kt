package codes.nibby.yi.common

import codes.nibby.yi.common.payload.SubmitMoveParameters

interface GameRulesHandler<NodeData> {

    fun playMove(gameModel: BoardGameModel<NodeData>, moveParameters: SubmitMoveParameters): MoveNode<NodeData>

    fun isLegalMove(moveParameters: SubmitMoveParameters): Boolean

}