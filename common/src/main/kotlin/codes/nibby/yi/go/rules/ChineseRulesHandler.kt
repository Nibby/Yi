package codes.nibby.yi.go.rules

class ChineseRulesHandler : GoGameRulesHandler() {

    override fun getKomi(): Float {
        return 7.5f
    }

    override fun allowSuicideMoves(): Boolean {
        return false
    }

}