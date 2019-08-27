package codes.nibby.yi.game.rules;

import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameOutcome;

public class ChineseGameRules extends StandardGameRules {

    @Override
    public boolean isValidMove(Game game, int color, int x, int y) {
        // TODO: Add rules later
        return true;
    }

    @Override
    public GameOutcome scoreGame(Game game){
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public float getKomi() {
        return 7.5f;
    }

}
