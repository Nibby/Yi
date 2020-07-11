package yi.editor.gui.board;

import org.jetbrains.annotations.Nullable;
import yi.core.go.GoGameModel;
import yi.core.go.GoGamePosition;
import yi.core.go.GoGameState;
import yi.core.go.GoStoneColor;

/**
 * Wraps the current {@link GoGameModel} and provide game information to other board classes.
 */
public final class GameBoardModel {

    private GoGameModel gameModel;
    private GoGameState currentGameState;

    GameBoardModel() { }

    public void setGameModel(GoGameModel gameModel) {
        this.gameModel = gameModel;
        update(gameModel);
    }

    public void update(GoGameModel gameModel) {
        if (this.gameModel != gameModel) {
            this.gameModel = gameModel;
        }

        this.currentGameState = this.gameModel.getCurrentGameState();
    }

    public GoGameState getCurrentGameState() {
        return currentGameState;
    }

    public GoGamePosition getCurrentGamePosition() {
        return getCurrentGameState().getGamePosition();
    }

    public int getBoardWidth() {
        return gameModel.getBoardWidth();
    }

    public int getBoardHeight() {
        return gameModel.getBoardHeight();
    }

    public GoStoneColor getNextTurnStoneColor() {
        return gameModel.getNextTurnStoneColor();
    }

    public int getCurrentMoveNumber() {
        return gameModel.getCurrentMoveNumber();
    }

    public int getNextMoveNumber() {
        return gameModel.getNextMoveNumber();
    }

    @Nullable GoGameModel getGameModel() {
        return gameModel;
    }
}
