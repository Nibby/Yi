package yi.models.go

/**
 * Represents the result of a request to submit a move to the game tree.
 *
 * @param validationResult Information pertaining to game rule compliance of the submitted
 * move, notably [MoveValidationResult.OK] meaning a legal move.
 *
 * @param moveNode The move node that is created from the request, may be null if the
 * request is in violation of game rules.
 *
 * @param isPlayed true if [moveNode] has already been appended to the game tree.
 *
 * @param isReusingExistingNode true if the next node in the game tree was identical to this
 * move, so rather than creating a new node, the identical move was re-used instead.
 */
class MoveSubmitResult constructor(val validationResult: MoveValidationResult,
                                   val moveNode: GameNode?,
                                   val isPlayed: Boolean,
                                   val isReusingExistingNode: Boolean = false) {

    override fun toString(): String {
        return "status: ${validationResult.name} played: $isPlayed node: ${moveNode.toString()}"
    }
}
