package codes.nibby.yi.go

import codes.nibby.yi.exception.GameRulesException

class SubmitMoveException : GameRulesException {
    constructor() : super()
    constructor(s: String?) : super(s)
}