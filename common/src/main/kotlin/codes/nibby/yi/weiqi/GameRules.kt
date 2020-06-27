package codes.nibby.yi.weiqi

enum class GameRules {
    CHINESE {
        override fun getRulesHandler() = ChineseRulesHandler()
    };

    abstract fun getRulesHandler(): GoRulesHandler;
}