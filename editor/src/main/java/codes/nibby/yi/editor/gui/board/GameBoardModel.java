package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.go.GoGameModel;
import codes.nibby.yi.go.GoGamePosition;
import codes.nibby.yi.go.GoGameState;

public final class GameBoardModel {

    private GoGameModel gameModel;

    private GoGameState currentGameState;

    public void initialize(GoGameModel gameModel) {
        this.gameModel = gameModel;
        update();
    }

    public void update() {
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
}
