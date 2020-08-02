package yi.component.board;

import yi.core.go.GameModel;

/**
 * Main controller class for {@link GameBoardCanvas} state. Receives UI and game events and manages the interaction
 * of subcomponents within the game module.
 */
public final class GameBoardManager {

    public final GameBoardModel model = new GameBoardModel();
    public final GameBoardSize size = new GameBoardSize();
    public final GameBoardView view = new GameBoardView();
    public final GameModelEditor edit = new GameModelEditor();

    private boolean debugMode = false;

    GameBoardManager() { }

    public void setGameModel(GameModel game) {
        model.setGameModel(game);
        edit.setEditable(true);
    }

    public void setBoardCanvasSize(double componentWidth, double componentHeight, GameModel game) {
        size.compute(componentWidth, componentHeight, game.getBoardWidth(), game.getBoardHeight(), view.coordinateLabelPosition);
    }

    public void onGameUpdate(GameModel game) {
        model.update(game);
    }

    boolean isDebugMode() {
        return debugMode;
    }

    void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
}
