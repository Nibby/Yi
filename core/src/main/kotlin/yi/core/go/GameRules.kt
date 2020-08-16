package yi.core.go

import yi.core.go.rules.ChineseRulesHandler
import yi.core.go.rules.GoGameRulesHandler
import java.util.*

enum class GameRules {

    CHINESE {
        override fun getRulesHandler() = ChineseRulesHandler()

        override fun isRuleset(rulesetName: String): Boolean = rulesetName.equals("chinese", ignoreCase = true)
    };

    abstract fun getRulesHandler(): GoGameRulesHandler

    /**
     * This method is often used to parse ruleset definition in text-form.
     *
     * @return true if the ruleset name is equal to this ruleset.
     */
    abstract fun isRuleset(rulesetName: String): Boolean

    companion object {
        fun parse(rulesetName: String): Optional<GameRules> {
            return Arrays.stream(values())
                    .filter { ruleset -> ruleset.isRuleset(rulesetName) }
                    .findAny()
        }
    }
}