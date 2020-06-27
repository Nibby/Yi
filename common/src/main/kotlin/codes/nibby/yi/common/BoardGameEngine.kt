package codes.nibby.yi.common

import codes.nibby.yi.common.payload.SubmitMoveParameters

abstract class BoardGameEngine<NodeData> constructor(val game: BoardGameModel<NodeData>) {

    /**
     * Requests a move be formally submitted to the game tree. This method assumes that the move is legal,
     * and will not perform legality checks. Use [isLegalMove] to verify this beforehand.
     *
     * @throws [GameRulesException] If game rules violation is detected during the submission process. The game tree will not be updated.
     */
    abstract fun submitMove(parameters: SubmitMoveParameters): Boolean

    open fun isLegalMove(parameters: SubmitMoveParameters): Boolean {
        return game.rules.isLegalMove(parameters)
    }

}