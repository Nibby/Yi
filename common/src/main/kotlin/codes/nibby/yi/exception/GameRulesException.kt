package codes.nibby.yi.exception

/**
 * Thrown when an attempted action is a violation of game rules.
 */
open class GameRulesException : IllegalStateException {

    constructor() : super()
    constructor(s: String?) : super(s)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)

}