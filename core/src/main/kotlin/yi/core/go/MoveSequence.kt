package yi.core.go

/**
 * A manager class that enables a sequence of moves to be played while checking each move is played successfully.
 */
class MoveSequence constructor(private val game: GameModel) {

    /**
     * Synonymous to [GameModel.submitNode], but introduces an additional state check to verify the move is submitted successfully.
     * Otherwise a [GameRulesException] is thrown to interrupt the sequence flow.
     */
    fun playMove(x: Int, y:Int): MoveSequence {
        val result = game.submitNode(x, y)
        if (result.validationResult != MoveValidationResult.OK) {
            throw GameRulesException(result.validationResult, "Cannot play move ($x, $y) at node position ${game.getCurrentNode().moveNumber}: ${result.validationResult}")
        }
        return this
    }

    /**
     * Synonymous to [GameModel.addAnnotationToCurrentMove]
     *
     * @see [GameModel.addAnnotationToCurrentMove]
     */
    fun annotate(annotation: Annotation): MoveSequence {
        game.addAnnotationToCurrentMove(annotation)
        return this
    }

    /**
     * Synonymous to [GameModel.submitPass].
     *
     * @see [GameModel.submitPass]
     */
    fun pass(): MoveSequence {
        game.submitPass()
        return this
    }

    /**
     * Synonymous to [GameModel.submitResign].
     *
     * @see [GameModel.submitResign]
     */
    fun resign(): MoveSequence {
        game.submitResign()
        return this
    }
}