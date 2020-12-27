package yi.models.go.rules

class ChineseRulesHandler : GameRulesHandler() {

    override fun getDefaultKomi(): Float {
        return 7.5f
    }

    override fun allowSuicideMoves(): Boolean {
        return false
    }

    override fun getInternalName(): String {
        return "Chinese"
    }

}