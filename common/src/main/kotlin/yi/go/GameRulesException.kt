package yi.go

/**
 * Thrown when an attempted action is a violation of game rules.
 */
open class GameRulesException constructor(val validationResult: MoveValidationResult, message: String?): IllegalStateException(message) {

    constructor(validationResult: MoveValidationResult) : this(validationResult, "")

}