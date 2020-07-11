package yi.core.go

import yi.core.rules.ChineseRulesHandler
import yi.core.rules.GoGameRulesHandler

enum class GoGameRules {

    CHINESE {
        override fun getRulesHandler() = ChineseRulesHandler()
    };

    abstract fun getRulesHandler(): GoGameRulesHandler

}