package codes.nibby.yi.weiqi

import codes.nibby.yi.common.GameRulesException

class SubmitMoveException : GameRulesException {
    constructor() : super()
    constructor(s: String?) : super(s)

    enum class Reason {
        POSITION_OUT_OF_BOUNDS,
        INVALID_KO_RECAPTURE,
        MOVE_SUICIDAL,
        POSITION_REPEAT,
        NON_EMPTY_INTERSECTION
    }
}