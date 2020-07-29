package yi.core.go

import java.util.*

/**
 * Represents the game position at a given [GameNode]. Yi's implementation of game position is based on an incremental model.
 * This means that each [GameNode] does not store the full game state at each position, rather, only the delta -- the change in
 * game state since the previous node. Therefore each node does not contain all the information to know the entire game state.
 *
 * To obtain the full game state at a given [GameNode], use [GameModel.getCurrentGameState]
 *
 * @param gameModel The game model the game state belongs to
 * @param gamePosition The board stone configuration represented by this state
 * @param representedNode The [GameNode] this game state represents
 * @param prisonersBlack Number of prisoners black has captured at this point
 * @param prisonersWhite Number of prisoners white has captured at this point
 * @param annotations The annotations present in this state
 */
class GameState(private val gameModel: GameModel,
                val gamePosition: GamePosition,
                val representedNode: GameNode,
                val prisonersWhite: Int,
                val prisonersBlack: Int,
                val annotations: Set<Annotation>,
                val stateHash: Long) {

    /**
     * @return The annotation at (x, y) if present, otherwise null.
     */
    fun getAnnotation(x: Int, y: Int): Optional<Annotation> {
        return annotations.stream().filter { annotation -> annotation.x == x && annotation.y == y }.findAny()
    }

}
