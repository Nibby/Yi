package codes.nibby.yi.game.rules;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameOutcome;

import java.util.ResourceBundle;

public class ChineseGameRules extends StandardGameRules {

    ChineseGameRules() {}

    @Override
    public GameOutcome scoreGame(Game game){
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public float getKomi() {
        return 7.5f;
    }

    @Override
    protected boolean isSuicideAllowed() {
        return false;
    }

    @Override
    public String getName() {
        ResourceBundle resource = Config.getLanguage().getResourceBundle("Global");
        return resource.getString("ruleset.chinese");
    }

}
