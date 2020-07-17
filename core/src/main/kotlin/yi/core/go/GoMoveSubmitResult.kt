package yi.core.go

import yi.core.common.GameNode

/**
 * Represents the result of a request to submit a move to the game tree.
 *
 * @param validationResult Information pertaining to game rule compliance of the submitted move, notably [GoMoveValidationResult.OK] meaning a legal move.
 * @param moveNode The move node that is created from the request, may be null if the request is in violation of game rules.
 * @param played true if [moveNode] has already been appended to the game tree.
 */
class GoMoveSubmitResult constructor(val validationResult: GoMoveValidationResult, val moveNode: GameNode<GoGameStateUpdate>?, val played: Boolean) {

    override fun toString(): String {
        return "status: ${validationResult.name} played: $played node: ${moveNode.toString()}"
    }
}
