package yi.core.go

/**
 * Represents the new changes (additions and/or deletions) since the last game position. This is the game state data
 * associated with each [GameNode].
 *
 * @param primaryMove The main move represented by the [GameNode]
 * @param captures The stones on the board that are captured
 * @param stateHash A hash code representing the current game state (not just the delta)
 * @param helperStoneUpdates Other changes in stone state on the game position (typically associated with adding or removing helper stones using an editor)
 */
class StateDelta(val type: Type,
                 val primaryMove: Stone?,
                 val captures: Set<Stone>,
                 val stateHash: Long,
                 val helperStoneUpdates: HashSet<Stone>,
                 val annotationsOnThisNode: HashSet<Annotation>) {

    enum class Type {
        MOVE_PLAYED,
        HELPER_STONE_EDIT,
        PASS,
        RESIGN,
        ROOT,

        EMPTY,
    }

    constructor() : this(Type.EMPTY, null, HashSet<Stone>(), 0, HashSet<Stone>(), HashSet<Annotation>()) {
        // Represents an empty state delta, the initial state for newly created game nodes
    }

    internal companion object Factory {
        fun forProposedMove(primaryMove: Stone, captures: HashSet<Stone>, stateHash: Long): StateDelta
                = StateDelta(Type.MOVE_PLAYED, primaryMove, captures, stateHash, HashSet(), HashSet())

        fun forRootNode(emptyPositionStateHash: Long): StateDelta
                = StateDelta(Type.ROOT, null, HashSet(), emptyPositionStateHash, HashSet(), HashSet())

        fun forPassMove(currentPositionStateHash: Long): StateDelta
                = StateDelta(Type.PASS, null, HashSet(), currentPositionStateHash, HashSet(), HashSet())

        fun forResignationMove(currentPositionStateHash: Long): StateDelta
                = StateDelta(Type.RESIGN, null, HashSet(), currentPositionStateHash, HashSet(), HashSet())
    }
}