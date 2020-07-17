package yi.core.go

/**
 * Handles the creation of [GoGameStateUpdate] depending on the context.
 */
internal object GoGameStateUpdateFactory {

    fun createForProposedMove(primaryMove: GoStoneData, captures: HashSet<GoStoneData>, stateHash: Long): GoGameStateUpdate
        = GoGameStateUpdate(GoGameStateUpdate.Type.MOVE_PLAYED, primaryMove, captures, stateHash, HashSet(), HashSet())

    fun createForRootNode(emptyPositionStateHash: Long): GoGameStateUpdate
        = GoGameStateUpdate(GoGameStateUpdate.Type.ROOT, null, HashSet(), emptyPositionStateHash, HashSet(), HashSet())

    fun createForPassMove(currentPositionStateHash: Long): GoGameStateUpdate
        = GoGameStateUpdate(GoGameStateUpdate.Type.PASS, null, HashSet(), currentPositionStateHash, HashSet(), HashSet())

    fun createForResignationMove(currentPositionStateHash: Long): GoGameStateUpdate
        = GoGameStateUpdate(GoGameStateUpdate.Type.RESIGN, null, HashSet(), currentPositionStateHash, HashSet(), HashSet())

}