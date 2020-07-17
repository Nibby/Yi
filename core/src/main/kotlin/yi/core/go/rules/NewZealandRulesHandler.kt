package yi.core.go.rules

class NewZealandRulesHandler : GoGameRulesHandler() {

    override fun getKomi(): Float {
        return 7F
    }

    override fun allowSuicideMoves(): Boolean {
        return true
    }

}