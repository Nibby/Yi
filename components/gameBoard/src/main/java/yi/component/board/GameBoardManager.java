package yi.component.board;

import org.jetbrains.annotations.NotNull;
import yi.core.go.GameModel;

import java.util.Objects;

/**
 * Main controller class for {@link GameBoardCanvas} state. Receives UI and game events and manages the interaction
 * of subcomponents within the game module.
 */
public final class GameBoardManager {

    public final GameBoardSize size = new GameBoardSize();
    public final GameBoardView view = new GameBoardView();
    public final GameModelEditor edit = new GameModelEditor();

    private boolean debugMode = false;
    private GameModel model = null;

    GameBoardManager() { }

    public void setGameModel(@NotNull GameModel gameModel) {
        this.model = Objects.requireNonNull(gameModel);
    }

    public void setBoardCanvasSize(double componentWidth, double componentHeight, GameModel game) {
        size.compute(componentWidth, componentHeight, game.getBoardWidth(), game.getBoardHeight(), view.coordinateLabelPosition);
    }

    public void onGameUpdate(GameModel game) {

    }

    boolean isDebugMode() {
        return debugMode;
    }

    void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public GameModel getGameModel() {
        return Objects.requireNonNull(model);
    }
}
