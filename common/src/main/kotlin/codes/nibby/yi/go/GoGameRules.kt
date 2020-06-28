package codes.nibby.yi.go

enum class GoGameRules {

    CHINESE {
        override fun getRulesHandler() = ChineseRulesHandler()
    };

    abstract fun getRulesHandler(): GoGameRulesHandler

}