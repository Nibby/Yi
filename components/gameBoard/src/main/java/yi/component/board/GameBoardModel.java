package yi.component.board;

import org.jetbrains.annotations.Nullable;
import yi.core.go.GameNode;
import yi.core.go.*;

import java.util.Set;

/**
 * Wraps the current {@link GameModel} and provide game information to other board classes.
 */
public final class GameBoardModel {

    private GameModel gameModel;
    private GameState currentGameState;

    GameBoardModel() { }

    public void setGameModel(GameModel gameModel) {
        this.gameModel = gameModel;
        update(gameModel);
    }

    public void update(GameModel gameModel) {
        if (this.gameModel != gameModel) {
            this.gameModel = gameModel;
        }

        this.currentGameState = this.gameModel.getCurrentGameState();
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    public GamePosition getCurrentGamePosition() {
        return getCurrentGameState().getGamePosition();
    }

    public int getBoardWidth() {
        return gameModel.getBoardWidth();
    }

    public int getBoardHeight() {
        return gameModel.getBoardHeight();
    }

    public StoneColor getNextTurnStoneColor() {
        return gameModel.getNextTurnStoneColor();
    }

    public int getCurrentMoveNumber() {
        return gameModel.getCurrentMoveNumber();
    }

    public int getNextMoveNumber() {
        return gameModel.getNextMoveNumber();
    }

    @Nullable GameModel getGameModel() {
        return gameModel;
    }

    public void toPreviousMove() {
        gameModel.toPreviousMove();
    }

    public void toNextMove() {
        gameModel.toNextMove();
    }

    public GameNode getCurrentMove() {
        return gameModel.getCurrentMove();
    }

    public void removeAnnotationsOnCurrentMove(int gridX, int gridY) {
        gameModel.removeAnnotationsFromCurrentMove(gridX, gridY);
    }

    public void addAnnotationToCurrentMove(Annotation annotation) {
        gameModel.addAnnotationOnCurrentMove(annotation);
    }

    public Set<Annotation> getAllAnnotationsOnCurrentMove() {
        return gameModel.getAnnotationsOnCurrentMove();
    }
}
