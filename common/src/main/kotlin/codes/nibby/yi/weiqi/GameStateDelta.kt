package codes.nibby.yi.weiqi

import codes.nibby.yi.common.MoveNode

/**
 * Represents the new changes (additions and/or deletions) since the last game position. This is the game state data
 * associated with each [MoveNode]
 *
 * @param primaryMove The main move represented by the [MoveNode]
 * @param captures The stones on the board that is captured
 * @param helpers The stones to be added to the board
 */
class GameStateDelta constructor(val primaryMove: Intersection,
                                 val captures: Set<Intersection>,
                                 val helpers: Set<Intersection>)