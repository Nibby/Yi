package yi.core

/**
 * A manager class that enables a sequence of moves to be played while checking each move is played successfully.
 */
class MoveSequence constructor(private val game: GoGameModel) {

    /**
     * Synonymous to [GoGameModel.playMove], but introduces an additional state check to verify the move is submitted successfully.
     * Otherwise a [GameRulesException] is thrown to interrupt the sequence flow.
     */
    fun playMove(x: Int, y:Int): MoveSequence {
        val result = game.playMove(x, y)
        if (result.validationResult != MoveValidationResult.OK) {
            throw GameRulesException(result.validationResult, "Cannot play move ($x, $y) at node position ${game.currentNode.getDistanceToRoot()}: ${result.validationResult}")
        }
        return this
    }

    /**
     * Synonymous to [GoGameModel.addAnnotationOnThisMove]
     *
     * @see [GoGameModel.addAnnotationOnThisMove]
     */
    fun annotate(annotation: Annotation): MoveSequence {
        game.addAnnotationOnThisMove(annotation)
        return this
    }

    /**
     * Synonymous to [GoGameModel.playPass].
     *
     * @see [GoGameModel.playPass]
     */
    fun pass(): MoveSequence {
        game.playPass()
        return this
    }

    /**
     * Synonymous to [GoGameModel.playResign].
     *
     * @see [GoGameModel.playResign]
     */
    fun resign(): MoveSequence {
        game.playResign()
        return this
    }
}