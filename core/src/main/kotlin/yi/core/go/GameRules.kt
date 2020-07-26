package yi.core.go

import yi.core.go.rules.ChineseRulesHandler
import yi.core.go.rules.GoGameRulesHandler

enum class GameRules {

    CHINESE {
        override fun getRulesHandler() = ChineseRulesHandler()
    };

    abstract fun getRulesHandler(): GoGameRulesHandler

}