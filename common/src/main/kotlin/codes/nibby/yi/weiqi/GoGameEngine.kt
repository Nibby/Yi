package codes.nibby.yi.weiqi

import codes.nibby.yi.common.BoardGameEngine
import codes.nibby.yi.common.payload.SubmitMoveParameters

class GoGameEngine(game: GoGameModel) : BoardGameEngine<GameStateDelta>(game) {

    override fun submitMove(parameters: SubmitMoveParameters): Boolean {
        return true
    }

}