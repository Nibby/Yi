package codes.nibby.yi.game.rules;

import codes.nibby.yi.io.GameParseException;
import jdk.jshell.spi.ExecutionControl;

/**
 * A collection of pre-defined game rules.
 *
 * @author Kevin Yang
 * Created on 28 August 2019
 */
public class GameRules {

    public static final ChineseGameRules CHINESE = new ChineseGameRules();

    public static IGameRules parse(String ruleset) throws GameParseException {
        ruleset = ruleset.toLowerCase();
        switch (ruleset) {
            case "chinese":
                return CHINESE;
            default:
                throw new GameParseException("Ruleset not implemented!");
        }
    }
}
