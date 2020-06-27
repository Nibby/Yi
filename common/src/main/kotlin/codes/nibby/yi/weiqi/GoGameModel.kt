package codes.nibby.yi.weiqi

import codes.nibby.yi.common.BoardGameModel
import codes.nibby.yi.common.MoveNode
import codes.nibby.yi.common.MoveTree

class GoGameModel(val boardWidth: Int, val boardHeight: Int, rules: GoGameRules)
    : BoardGameModel<GameStateDelta>(rules.getRulesHandler(), MoveTree(MoveNode())) {



}