package yi.editor.gui.board;

import yi.core.go.GoGameModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Main controller class for {@link GameBoardCanvas} state. Receives UI and game events and manages the interaction
 * of subcomponents within the game module.
 */
public final class GameBoardManager {

    public final GameBoardModel model = new GameBoardModel();
    public final GameBoardSize size = new GameBoardSize();
    public final GameBoardView view = new GameBoardView();
    public final GameBoardEditor edit = new GameBoardEditor();

    private List<Runnable> gameUpdateListeners = new ArrayList<>();

    GameBoardManager() { }

    void onGameModelSet(GoGameModel game) {
        model.setGameModel(game);
        edit.setEditable(true);
    }

    void onBoardSizeUpdate(double componentWidth, double componentHeight, GoGameModel game) {
        size.compute(componentWidth, componentHeight, game.getBoardWidth(), game.getBoardHeight(), view.getCoordinateLabelPosition());
    }

    void onGameUpdate(GoGameModel game) {
        model.update(game);
        fireGameUpdateEvent();
    }

    void addGameUpdateListener(Runnable listener) {
        gameUpdateListeners.add(listener);
    }

    void fireGameUpdateEvent() {
        gameUpdateListeners.forEach(Runnable::run);
    }
}
