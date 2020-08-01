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

    GameBoardManager() { }

    void onGameModelSet(GameModel game) {
        model.setGameModel(game);
        edit.setEditable(true);
    }

    void onBoardSizeUpdate(double componentWidth, double componentHeight, GameModel game) {
        size.compute(componentWidth, componentHeight, game.getBoardWidth(), game.getBoardHeight(), view.coordinateLabelPosition);
    }

    void onGameUpdate(GameModel game) {
        model.update(game);
    }
}
