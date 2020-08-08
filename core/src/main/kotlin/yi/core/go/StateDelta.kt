package yi.core.go

/**
 * Primary data storage for each [GameNode]. Only the game node class should interact with the state delta.
 *
 * Represents the new changes (additions and/or deletions) since the last game position. This is the game state data
 * associated with each [GameNode].
 */
class StateDelta {

    internal val type: GameNodeType
    internal val primaryMove: Stone?
    internal val captures: Set<Stone>
    internal var stateHash: Long
    internal val stoneEdits: HashSet<Stone>
    internal val annotations: HashSet<Annotation>

    /**
     * @param primaryMove The main move represented by the [GameNode], this is added to the stone delta if not null.
     * @param captures The stones on the board that are captured
     * @param stateHash A hash code representing the current game state (not just the delta)
     * @param stoneDelta Other changes in stone state on the game position (typically associated with adding or removing helper stones using an editor)
     * @param annotations List of annotations to be shown on the node with this delta
     */
    internal constructor(type: GameNodeType,
                         primaryMove: Stone?,
                         captures: Set<Stone>,
                         stateHash: Long,
                         stoneDelta: HashSet<Stone>,
                         annotations: HashSet<Annotation>) {

        this.type = type
        this.primaryMove = primaryMove
        this.captures = captures
        this.stateHash = stateHash
        this.stoneEdits = stoneDelta
        this.annotations = annotations

        this.primaryMove?.let {
            stoneDelta.add(it)
        }
    }

    internal companion object Factory {
        /**
         * Instantiates a [StateDelta] that encapsulates information for a primary game move.
         */
        fun forProposedMove(primaryMove: Stone, captures: HashSet<Stone>, stateHash: Long): StateDelta
                = StateDelta(GameNodeType.MOVE_PLAYED, primaryMove, captures, stateHash, HashSet(), HashSet())

        /**
         * Instantiates a default [StateDelta] for the root node of the game tree.
         */
        internal fun forRootNode(emptyPositionStateHash: Long): StateDelta
                = StateDelta(GameNodeType.ROOT, null, HashSet(), emptyPositionStateHash, HashSet(), HashSet())

        /**
         * Instantiates a [StateDelta] to represent a pass.
         */
        fun forPassMove(currentPositionStateHash: Long): StateDelta
                = StateDelta(GameNodeType.PASS, null, HashSet(), currentPositionStateHash, HashSet(), HashSet())

        /**
         * Instantiates a [StateDelta] to represent a resignation.
         */
        fun forResignationMove(currentPositionStateHash: Long): StateDelta
                = StateDelta(GameNodeType.RESIGN, null, HashSet(), currentPositionStateHash, HashSet(), HashSet())

        /**
         * Instantiates an empty [StateDelta] to store updates to the game position through
         * manual stone edits.
         */
        fun forStoneEdit(currentPositionStateHash: Long): StateDelta
                = StateDelta(GameNodeType.STONE_EDIT, null, HashSet(), currentPositionStateHash, HashSet(), HashSet())
    }
}