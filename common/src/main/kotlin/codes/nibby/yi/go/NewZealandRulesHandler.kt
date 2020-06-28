package codes.nibby.yi.go

class NewZealandRulesHandler : GoGameRulesHandler() {

    override fun getKomi(): Float {
        return 7F
    }

    override fun allowSuicideMoves(): Boolean {
        return true
    }

}