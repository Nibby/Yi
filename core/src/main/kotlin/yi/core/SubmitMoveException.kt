package yi.core

/**
 * An exception thrown to represent a submitted move is illegal and cannot be played.
 */
class SubmitMoveException : GoGameRulesException {
    constructor(validationResult: GoMoveValidationResult, message: String?) : super(validationResult, message)
    constructor(validationResult: GoMoveValidationResult) : super(validationResult)
}