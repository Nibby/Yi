package codes.nibby.yi.go

import codes.nibby.yi.common.MoveNode

/**
 * Represents the new changes (additions and/or deletions) since the last game position. This is the game state data
 * associated with each [MoveNode]
 *
 * @param primaryMove The main move represented by the [MoveNode]
 * @param captures The stones on the board that are captured
 * @param stateHash A hash code representing the current game state (not just the delta)
 * @param helperStoneUpdates Other changes in stone state on the game position (typically associated with adding or removing helper stones using an editor)
 */
class GameStateUpdate(val type: Type,
                      val primaryMove: StoneData?,
                      val captures: Set<StoneData>,
                      val stateHash: Long,
                      val helperStoneUpdates: HashSet<StoneData>,
                      val annotationsOnThisNode: HashSet<Annotation>) {

    enum class Type {
        MOVE_PLAYED,
        HELPER_STONE_EDIT,
        PASS,
        RESIGN,
        ROOT
    }

}