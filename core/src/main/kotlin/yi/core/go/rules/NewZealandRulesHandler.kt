package yi.core.go.rules

class NewZealandRulesHandler : GoGameRulesHandler() {

    override fun getDefaultKomi(): Float {
        return 7F
    }

    override fun allowSuicideMoves(): Boolean {
        return true
    }

    override fun getInternalName(): String {
        return "New Zealand"
    }

}