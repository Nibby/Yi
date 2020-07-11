package codes.nibby.go

/**
 * Represents the game position at a given [MoveNode]. Yi's implementation of game position is based on an incremental model.
 * This means that each [MoveNode] does not store the full game state at each position, rather, only the delta -- the change in
 * game state since the previous node. Therefore each node does not contain all the information to know the entire game state.
 *
 * To obtain the full game state at a given [MoveNode], use [GoGameStateHelper]
 *
 * @param gameModel The game model the game state belongs to
 * @param gamePosition The board stone configuration represented by this state
 * @param representedNode The [MoveNode] this game state represents
 * @param prisonersBlack Number of prisoners black has captured at this point
 * @param prisonersWhite Number of prisoners white has captured at this point
 * @param annotations The annotations present in this state
 *
 * @see GoGameStateHelper
 */
class GoGameState(private val gameModel: GoGameModel,
                  val gamePosition: GoGamePosition,
                  val representedNode: MoveNode<GameStateUpdate>,
                  val prisonersWhite: Int,
                  val prisonersBlack: Int,
                  val annotations: Set<Annotation>,
                  val stateHash: Long)
