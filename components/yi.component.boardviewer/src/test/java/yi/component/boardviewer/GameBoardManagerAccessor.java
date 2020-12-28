package yi.component.boardviewer;

import org.jetbrains.annotations.NotNull;
import yi.core.go.GameModel;

public final class GameBoardManagerAccessor {

    private GameBoardManagerAccessor() {

    }

    public static void setGameModel(GameBoardManager manager, @NotNull GameModel newModel) {
        manager.setGameModel(newModel);
    }

}
