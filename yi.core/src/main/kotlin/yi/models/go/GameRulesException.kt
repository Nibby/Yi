package yi.models.go

/**
 * Thrown when an attempted action is a violation of game rules.
 */
open class GameRulesException constructor(validationResult: MoveValidationResult, message: String?)
    : IllegalStateException("Validation Result: $validationResult, Message: $message") {

    constructor(validationResult: MoveValidationResult) : this(validationResult, "")

}