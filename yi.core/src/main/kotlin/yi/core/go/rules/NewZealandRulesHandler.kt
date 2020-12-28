package yi.core.go.rules

class NewZealandRulesHandler : GameRulesHandler() {

    override fun getDefaultKomi(): Float {
        return 7f
    }

    override fun allowSuicideMoves(): Boolean {
        return true
    }

    override fun getInternalName(): String {
        return "New Zealand"
    }

}