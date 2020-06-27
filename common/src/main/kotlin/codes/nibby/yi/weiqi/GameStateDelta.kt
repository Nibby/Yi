package codes.nibby.yi.weiqi

import codes.nibby.yi.common.MoveNode

/**
 * Represents the new changes (additions and/or deletions) since the last game position. This is the game state data
 * associated with each [MoveNode]
 *
 * @param primaryMove The main move represented by the [MoveNode]
 * @param captures The stones on the board that is captured
 * @param helpersAdded The stones to be added to the board
 */
class GameStateDelta constructor(val primaryMove: StoneData?,
                                 val captures: Set<StoneData>,
                                 val helpersAdded: Set<StoneData>,
                                 val helpersRemoved: Set<Int>,
                                 val illegalKoMove: Int,
                                 val illegalRepeatMove: Int)