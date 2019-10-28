package codes.nibby.yi.game.rules;

import codes.nibby.yi.io.GameParseException;

/**
 * A collection of pre-defined game rules.
 *
 * @author Kevin Yang
 * Created on 28 August 2019
 */
public class GameRules {

    public static final ChineseGameRules CHINESE = new ChineseGameRules();

    public static IGameRules parse(String ruleset) throws GameParseException {
        ruleset = ruleset.toUpperCase();
        switch (ruleset) {
            case "CHINESE":
                return CHINESE;
            default:
                throw new GameParseException("Ruleset not implemented: " + ruleset);
        }
    }
}
