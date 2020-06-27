package codes.nibby.yi.weiqi

import codes.nibby.yi.common.GameRulesException

class SubmitMoveException : GameRulesException {
    constructor() : super()
    constructor(s: String?) : super(s)
}