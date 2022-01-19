package codes.nibby.yi.app.utilities;

import codes.nibby.yi.app.framework.global.GlobalHelper;
import codes.nibby.yi.models.GameModel;
import codes.nibby.yi.models.StandardGameRules;

public final class GameModelUtilities {

    private GameModelUtilities() {
        // No instantiation
    }

    public static GameModel createGameModel() {
        return createGameModel(19, 19, StandardGameRules.CHINESE);
    }

    public static GameModel createGameModel(int width, int height, StandardGameRules ruleset) {
        return createGameModel(width, height, ruleset, ruleset.getRulesHandler().getDefaultKomi(0));
    }

    private static GameModel createGameModel(int width, int height, StandardGameRules ruleset,
                                             float customKomi) {
        var model = new GameModel(width, height, ruleset);
        model.getInfo().setKomi(customKomi);
        model.getInfo().setApplicationName(GlobalHelper.getProgramName() + " " + GlobalHelper.getVersion());

        return model;
    }

}
