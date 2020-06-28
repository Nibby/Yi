package codes.nibby.yi.go

import codes.nibby.yi.go.rules.ChineseRulesHandler
import codes.nibby.yi.go.rules.GoGameRulesHandler

enum class GoGameRules {

    CHINESE {
        override fun getRulesHandler() = ChineseRulesHandler()
    };

    abstract fun getRulesHandler(): GoGameRulesHandler

}