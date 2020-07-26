package yi.core.go

/**
 * An exception thrown to represent a submitted move is illegal and cannot be played.
 */
class IllegalMoveException : GameRulesException {
    constructor(validationResult: MoveValidationResult, message: String?) : super(validationResult, message)
    constructor(validationResult: MoveValidationResult) : super(validationResult)
}