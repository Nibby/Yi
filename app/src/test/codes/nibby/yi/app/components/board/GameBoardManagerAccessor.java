package codes.nibby.yi.app.components.board;

import org.jetbrains.annotations.NotNull;
import codes.nibby.yi.models.GameModel;

public final class GameBoardManagerAccessor {

    private GameBoardManagerAccessor() {

    }

    public static void setGameModel(GameBoardManager manager, @NotNull GameModel newModel) {
        manager.setGameModel(newModel);
    }

}
