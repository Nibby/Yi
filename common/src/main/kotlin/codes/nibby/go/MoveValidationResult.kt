package codes.nibby.go

/**
 * Represents the outcome of a result validation of a proposed move. Constants in this enum determine
 * whether a proposed game move is conforming to, or in violation of, the game rules. If a proposed move
 * is acceptable, [OK] is returned. Otherwise, another value is returned detailing the reason for failure.
 */
enum class MoveValidationResult {

    /** The move is legal and playable. */
    OK,

    /** The move is not playable because the proposed move is out of board grid boundaries. */
    ERROR_POSITION_OUT_OF_BOUNDS,

    /** The move is not playable because it is an attempt to immediately re-capture the ko. */
    ERROR_KO_RECAPTURE,

    /**
     * The move is not playable because the proposed move causes an allied group to be captured.
     * However, current game rules do not allow suicide.
     */
    ERROR_MOVE_SUICIDAL,

    /**
     * If the move is played, it would result in a position that has already occurred previously in the game history.
     * Therefore it is prohibited.
     */
    ERROR_POSITION_REPEAT,

    /** The move is not playable because another stone exists in that location. */
    ERROR_NON_EMPTY_INTERSECTION,

    /** The move is not playable because the game rules expect a player of different color to play this turn. */
    ERROR_WRONG_STONE_COLOR_THIS_TURN

}