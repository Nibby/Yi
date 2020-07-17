package yi.core.go

import yi.core.go.rules.ChineseRulesHandler
import yi.core.go.rules.GoGameRulesHandler

enum class GoGameRules {

    CHINESE {
        override fun getRulesHandler() = ChineseRulesHandler()
    };

    abstract fun getRulesHandler(): GoGameRulesHandler

}