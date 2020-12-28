package yi.core.go

/**
 * Thrown when an error is encountered while parsing documents as [GameModel].
 * Usually caused by malformed game data.
 */
class GameParseException(message: String, cause: Throwable?) : Exception(message, cause) {

    constructor(message: String) : this(message, null)

}