package yi.core.go.rules

class ChineseRulesHandler : GoGameRulesHandler() {

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