package codes.nibby.yi.weiqi

import codes.nibby.yi.common.MoveNode

/**
 * Responsible for the creation of [MoveNode] and [GameStateDelta] for [GoGameModel]. In other words, the class manages the logic around
 * move submission in the game of Go.
 */
internal object GoMoveHelper {

    /**
     * Same as [createMoveNode] except the new move node is created at the current position defined by [gameModel]
     *
     * @param gameModel The game to create the new move for.
     * @param validateAgainstRules Whether to validate the move against the game rules first. If this is non-null, and the proposed move is not
     *                             in compliance with game rules, then no new node be created.
     * @param proposedMove Information pertaining to the proposed move, see [StoneData]
     */
    fun createMoveNode(gameModel: GoGameModel, validateAgainstRules: Boolean, proposedMove: StoneData)
            : Pair<MoveValidationResult, MoveNode<GameStateDelta>?> {

        return createMoveNode(gameModel, gameModel.currentNode, validateAgainstRules, proposedMove)
    }

    /**
     * Creates a new move node from the proposed move at a specified game position. If [validateBeforeCreate] is true, this method will first check
     * if the proposed move complies with the game rules before creating the new node.
     *
     * This resulting node can then be submitted to the game tree through [GoGameEngine].
     *
     * @param gameModel The game to create the new move for.
     * @param currentPosition The parent node of the newly created move node. In other words, the game state on which to play the new move.
     * @param validateBeforeCreate Whether to validate the move against the game rules first. If this is non-null, and the proposed move is not in compliance with game rules,
     *                             then no new node be created.
     * @param proposedMove Information pertaining to the proposed move, see [StoneData]
     */
    fun createMoveNode(gameModel: GoGameModel, currentPosition: MoveNode<GameStateDelta>, validateBeforeCreate: Boolean, proposedMove: StoneData)
            : Pair<MoveValidationResult, MoveNode<GameStateDelta>?> {
        val captures = HashSet<StoneData>()

        var validationResult = MoveValidationResult.OK
        val delta: GameStateDelta?

        if (validateBeforeCreate) {
            val validationAndDelta = validateAndCreateDelta(gameModel, currentPosition, proposedMove)
            validationResult = validationAndDelta.first

            if (validationResult != MoveValidationResult.OK) {
                return Pair(validationResult, null)
            }

            delta = validationAndDelta.second
        } else {
            delta = GameStateDelta(proposedMove, captures, HashSet(), HashSet(), -1, -1)
        }

        return Pair(validationResult, MoveNode(delta))
    }

    /**
     * Validates the [proposedMove] against the game rules and if the move is legal (as given by [MoveValidationResult.OK]), returns a [GameStateDelta]
     * representing the game state updates caused by playing this move on the game board.
     *
     * @param gameModel Game information this move belongs to
     * @param position The position at which the new move will be validated
     * @param proposedMove Information pertaining to the proposed move, see [StoneData]
     */
    fun validateAndCreateDelta(gameModel: GoGameModel, position: MoveNode<GameStateDelta>, proposedMove: StoneData): Pair<MoveValidationResult, GameStateDelta> {
        TODO("Not yet implemented")
    }

}