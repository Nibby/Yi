package codes.nibby.yi.go.rules

import codes.nibby.yi.go.rules.GoGameRulesHandler

class NewZealandRulesHandler : GoGameRulesHandler() {

    override fun getKomi(): Float {
        return 7F
    }

    override fun allowSuicideMoves(): Boolean {
        return true
    }

}