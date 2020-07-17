package yi.core.go

/**
 * An exception thrown to represent a submitted move is illegal and cannot be played.
 */
class IllegalMoveException : GoGameRulesException {
    constructor(validationResult: GoMoveValidationResult, message: String?) : super(validationResult, message)
    constructor(validationResult: GoMoveValidationResult) : super(validationResult)
}