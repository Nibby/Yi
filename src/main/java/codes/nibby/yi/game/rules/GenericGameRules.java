package codes.nibby.yi.game.rules;

import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameOutcome;

public class GenericGameRules extends StandardGameRules {

    @Override
    protected boolean isSuicideAllowed() {
        return false;
    }

    @Override
    public GameOutcome scoreGame(Game game) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public float getKomi() {
        return 6.5f;
    }

    @Override
    public String getName() {
        return "generic";
    }
}
