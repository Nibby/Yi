package codes.nibby.yi.weiqi

internal object GameStateUpdateFactory {

    fun createPrimaryMoveUpdate(primaryMove: StoneData, captures: HashSet<StoneData>, stateHash: Long): GameStateUpdate
        = GameStateUpdate(GameStateUpdate.Type.MOVE_PLAYED, primaryMove, captures, stateHash, HashSet())

    fun createRootUpdate(): GameStateUpdate
        = GameStateUpdate(GameStateUpdate.Type.ROOT, null, HashSet(), 0, HashSet())

}