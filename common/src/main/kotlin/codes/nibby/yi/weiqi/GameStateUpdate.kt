package codes.nibby.yi.weiqi

import codes.nibby.yi.common.MoveNode

/**
 * Represents the new changes (additions and/or deletions) since the last game position. This is the game state data
 * associated with each [MoveNode]
 *
 * @param primaryMove The main move represented by the [MoveNode]
 * @param captures The stones on the board that are captured
 * @param stateHash A hash code representing the current game state (not just the delta)
 */
class GameStateUpdate(val type: Type,
                      val primaryMove: StoneData?,
                      val captures: Set<StoneData>,
                      val stateHash: Long,
                      val helperStoneUpdates: HashSet<StoneData>) {

    fun getStoneUpdates(): Set<StoneData> {
        val result = HashSet<StoneData>()

        primaryMove?.let { result.add(it) }
        result.addAll(captures)
        result.addAll(helperStoneUpdates)

        return result
    }

    enum class Type {
        MOVE_PLAYED,
        HELPER_STONE_EDIT,
        PASS,
        RESIGN,
        ROOT
    }

}