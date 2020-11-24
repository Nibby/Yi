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

    public void setBoardCanvasSize(double componentWidth, double componentHeight, GameModel game) {
        size.compute(componentWidth, componentHeight, game.getBoardWidth(),
                game.getBoardHeight(), view.coordinateLabelPosition);
    }

    public void onGameUpdate(GameModel game) {

    }

    boolean isDebugMode() {
        return debugMode;
    }

    void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void setGameModel(@NotNull GameModel gameModel) {
        // TODO: This method was made public because of test cases. Consider using
        //       jigsaw project structure to sort out the method visibilities.
        this.model = Objects.requireNonNull(gameModel);
    }

    public @NotNull GameModel getGameModel() {
        Objects.requireNonNull(model, "No game model set. Use setGameModel() once " +
                "before calling getGameModel()");
        return model;
    }

    public boolean hasGameModel() {
        return model != null;
    }
}
