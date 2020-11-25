package yi.editor.utilities;

import yi.core.go.GameModel;
import yi.core.go.GameRules;
import yi.editor.Yi;

public final class GameModelUtilities {

    private GameModelUtilities() {
        // No instantiation
    }

    public static GameModel createGameModel() {
        return createGameModel(19, 19, GameRules.CHINESE);
    }

    public static GameModel createGameModel(int width, int height, GameRules ruleset) {
        return createGameModel(width, height, ruleset, ruleset.getRulesHandler().getDefaultKomi());
    }

    private static GameModel createGameModel(int width, int height, GameRules ruleset,
                                             float customKomi) {
        var model = new GameModel(width, height, ruleset);
        model.setKomi(customKomi);
        model.setApplicationName(Yi.getProgramName() + " " + Yi.getVersion());

        return model;
    }

}
