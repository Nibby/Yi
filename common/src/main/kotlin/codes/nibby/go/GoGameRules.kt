package codes.nibby.go

import codes.nibby.go.rules.ChineseRulesHandler
import codes.nibby.go.rules.GoGameRulesHandler

enum class GoGameRules {

    CHINESE {
        override fun getRulesHandler() = ChineseRulesHandler()
    };

    abstract fun getRulesHandler(): GoGameRulesHandler

}