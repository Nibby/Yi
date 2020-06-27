package codes.nibby.yi.weiqi

import codes.nibby.yi.common.BoardGameEngine
import codes.nibby.yi.common.MoveNode

class GoGameEngine(game: GoGameModel) : BoardGameEngine<GameStateDelta>(game) {

    /**
     * Represents the result of a request to submit a move to the game tree.
     *
     * @param validationResult Information pertaining to game rule compliance of the submitted move, notably [MoveValidationResult.OK] meaning a legal move.
     * @param newNode The move node that is created from the request, may be null if the request is in violation of game rules.
     * @param played true if [newNode] has already been appended to the game tree.
     */
    class MoveSubmitResult constructor(val validationResult: MoveValidationResult, val newNode: MoveNode<GameStateDelta>?, val played: Boolean)

    /**
     * Forcefully submit a move to the game tree without validating it against the game rules. Use this method with prudence, as
     * it may result in an erroneous game state.
     */
    fun forcePlayMove(game: GoGameModel, x: Int, y: Int, color: GoStoneColor): MoveSubmitResult {
        val validationAndNewNode = GoMoveHelper.createMoveNode(game, false, StoneData(x, y, color))
        val newNode: MoveNode<GameStateDelta>? = validationAndNewNode.second
        playMove(newNode!!)

        return MoveSubmitResult(MoveValidationResult.OK, newNode, true)
    }

    /**
     * First checks if the move can be played at the current game position in compliance with the game rules. If successful,
     * appends a new node to the game tree.
     *
     * @return The result of the request. See [MoveSubmitResult] for more information.
     */
    fun validateAndPlayMove(game: GoGameModel, x: Int, y: Int, color: GoStoneColor): MoveSubmitResult {
        val validationAndNewNode = GoMoveHelper.createMoveNode(game, true, StoneData(x, y, color))

        val validationResult = validationAndNewNode.first
        val newNode: MoveNode<GameStateDelta>? = validationAndNewNode.second

        if (validationResult == MoveValidationResult.OK) {
            playMove(newNode!!) // New node should not be null if validation result checks out
        }
        return MoveSubmitResult(validationResult, newNode, validationResult == MoveValidationResult.OK)
    }

    /**
     * Checks if a hypothetical move can be played at the current game position. This will only test the move, but not create a node.
     *
     * @return [MoveValidationResult.OK] if the move can be played in compliance with game rules, otherwise other values representing
     *         reason for game rules violation.
     */
    fun validateMove(game: GoGameModel, x: Int, y: Int, color: GoStoneColor): MoveValidationResult {
        val proposedMove = StoneData(x, y, color)
        val validationAndDelta = GoMoveHelper.validateAndCreateDelta(game, game.currentNode, proposedMove)

        return validationAndDelta.first
    }

    /**
     * Appends the move node after the current position in the game tree. If the
     * node has not been validated by [validateMove], it may corrupt the game state.
     */
    private fun playMove(newNode: MoveNode<GameStateDelta>) {
        game.appendNode(newNode)
    }

}