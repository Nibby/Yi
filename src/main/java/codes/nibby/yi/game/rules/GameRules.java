package codes.nibby.yi.game.rules;

import codes.nibby.yi.io.GameParseException;

/**
 * A collection of pre-defined game rules.
 *
 * @author Kevin Yang
 * Created on 28 August 2019
 */
public class GameRules {

    public static final GenericGameRules GENERIC = new GenericGameRules();
    public static final ChineseGameRules CHINESE = new ChineseGameRules();

    public static IGameRules getRuleset(String ruleset, boolean allowGeneric) throws GameParseException {
        ruleset = ruleset.toUpperCase();
        switch (ruleset) {
            case "CHINESE":
                return CHINESE;
            default:
                if (!allowGeneric)
                    throw new GameParseException("Unsupported ruleset: " + ruleset);
                else
                    return GENERIC;
        }
    }
}
