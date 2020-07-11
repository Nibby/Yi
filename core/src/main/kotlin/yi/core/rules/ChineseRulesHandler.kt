package yi.core.rules

class ChineseRulesHandler : GoGameRulesHandler() {

    override fun getKomi(): Float {
        return 7.5f
    }

    override fun allowSuicideMoves(): Boolean {
        return false
    }

}