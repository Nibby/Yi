package codes.nibby.yi.go

import codes.nibby.yi.exception.GameRulesException

/**
 * An exception thrown to represent a submitted move is illegal and cannot be played.
 */
class SubmitMoveException : GameRulesException {
    constructor() : super()
    constructor(s: String?) : super(s)
}