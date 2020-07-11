package yi.core.go

/**
 * Thrown when an attempted action is a violation of game rules.
 */
open class GoGameRulesException constructor(val validationResult: GoMoveValidationResult, message: String?): IllegalStateException(message) {

    constructor(validationResult: GoMoveValidationResult) : this(validationResult, "")

}