package codes.nibby.yi.models

/**
 * Indicates the primary information stored by the [GameNode]. This is set by the factory
 * method used to instantiate the [StateDelta]. See the documentation of individual values
 * for how each are created.
 */
enum class GameNodeType {
    /**
     * Represents information of a game move that has been played
     * under normal game rule conditions. Game moves played without rule validation also
     * belong in this category.
     *
     * Created using [StateDelta.forProposedMove].
     */
    MOVE_PLAYED,

    /**
     * Represents intersection state changes (pertaining to stones on the board) since the
     * last node.
     *
     * Created using [StateDelta.forStoneEdit].
     */
    STONE_EDIT,

    /**
     * Represents that the player has passed in this turn.
     *
     * Created using [StateDelta.forPassMove]
     */
    PASS,

    /**
     * Default type given to the root of the game tree. This should not be used anywhere
     * else.
     *
     * Created using [StateDelta.forRootNode]
     */
    ROOT,
}