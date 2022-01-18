package codes.nibby.yi.models.rules

class NewZealandRulesHandler : GameRulesHandler() {

    override fun getDefaultKomi(handicap: Int): Float {
        return when {
            handicap == 0 -> 7f
            else -> 0f
        }
    }

    override fun allowSuicideMoves(): Boolean {
        return true
    }

    override fun getInternalName(): String {
        return "New Zealand"
    }

}