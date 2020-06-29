package codes.nibby.yi.go

import codes.nibby.yi.common.MoveNode

/**
 * Represents the result of a request to submit a move to the game tree.
 *
 * @param validationResult Information pertaining to game rule compliance of the submitted move, notably [MoveValidationResult.OK] meaning a legal move.
 * @param moveNode The move node that is created from the request, may be null if the request is in violation of game rules.
 * @param played true if [moveNode] has already been appended to the game tree.
 */
class MoveSubmitResult constructor(val validationResult: MoveValidationResult, val moveNode: MoveNode<GameStateUpdate>?, val played: Boolean) {

    override fun toString(): String {
        return "status: ${validationResult.name} played: $played node: ${moveNode.toString()}"
    }
}
