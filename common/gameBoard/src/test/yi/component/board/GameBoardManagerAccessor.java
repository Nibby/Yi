package yi.component.board;

import org.jetbrains.annotations.NotNull;
import yi.models.go.GameModel;

public final class GameBoardManagerAccessor {

    private GameBoardManagerAccessor() {

    }

    public static void setGameModel(GameBoardManager manager, @NotNull GameModel newModel) {
        manager.setGameModel(newModel);
    }

}
