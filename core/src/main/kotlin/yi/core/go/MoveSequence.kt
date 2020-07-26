package yi.core.go

/**
 * A manager class that enables a sequence of moves to be played while checking each move is played successfully.
 */
class MoveSequence constructor(private val game: GameModel) {

    /**
     * Synonymous to [GameModel.playMove], but introduces an additional state check to verify the move is submitted successfully.
     * Otherwise a [GameRulesException] is thrown to interrupt the sequence flow.
     */
    fun playMove(x: Int, y:Int): MoveSequence {
        val result = game.playMove(x, y)
        if (result.validationResult != MoveValidationResult.OK) {
            throw GameRulesException(result.validationResult, "Cannot play move ($x, $y) at node position ${game.getCurrentMove().moveNumber}: ${result.validationResult}")
        }
        return this
    }

    /**
     * Synonymous to [GameModel.addAnnotationOnCurrentMove]
     *
     * @see [GameModel.addAnnotationOnCurrentMove]
     */
    fun annotate(annotation: Annotation): MoveSequence {
        game.addAnnotationOnCurrentMove(annotation)
        return this
    }

    /**
     * Synonymous to [GameModel.playPass].
     *
     * @see [GameModel.playPass]
     */
    fun pass(): MoveSequence {
        game.playPass()
        return this
    }

    /**
     * Synonymous to [GameModel.playResign].
     *
     * @see [GameModel.playResign]
     */
    fun resign(): MoveSequence {
        game.playResign()
        return this
    }
}