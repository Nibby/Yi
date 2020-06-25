package codes.nibby.yi.common.ruleset

enum class Ruleset {

    CHINESE {
        override fun getHandler(): RulesetHandler {
            TODO("Not yet implemented")
        }
    };

    abstract fun getHandler(): RulesetHandler

}