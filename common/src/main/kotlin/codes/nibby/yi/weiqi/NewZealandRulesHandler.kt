package codes.nibby.yi.weiqi

class NewZealandRulesHandler : GoGameRulesHandler() {

    override fun getKomi(): Float {
        return 7F
    }

    override fun allowSuicideMoves(): Boolean {
        return true
    }

}