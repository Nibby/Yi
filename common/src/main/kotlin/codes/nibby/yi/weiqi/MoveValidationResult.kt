package codes.nibby.yi.weiqi

enum class MoveValidationResult {

    OK,
    POSITION_OUT_OF_BOUNDS,
    INVALID_KO_RECAPTURE,
    MOVE_SUICIDAL,
    POSITION_REPEAT,
    NON_EMPTY_INTERSECTION

}