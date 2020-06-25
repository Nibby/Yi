package codes.nibby.yi.common

import codes.nibby.yi.common.ruleset.Ruleset

/**
 * Represents a game record in its entirety, including board state, annotations, metadata
 *
 * @param boardWidth Number of horizontal intersections on the game board.
 * @param boardHeight Number of vertical intersections on the game board.
 * @param ruleset Game rules being used for this game, determines which moves are allowed to be played, as well as scoring results.
 */
class GoGame constructor(val boardWidth: Int, val boardHeight: Int, val ruleset: Ruleset) {

    /** Top level node in the game tree. */
    private val rootNode: GameNode = GameNode()

    init {
        rootNode.markAsRoot()
    }


}