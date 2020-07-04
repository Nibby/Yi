package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.go.GoGameModel;

/**
 * Main controller class for {@link GameBoardCanvas} state. Receives UI and game events and manages the interaction
 * of subcomponents within the game module.
 */
final class GameBoardManager {

    final GameBoardModel model = new GameBoardModel();
    final GameBoardSize size = new GameBoardSize();
    final GameBoardView view = new GameBoardView();
    final GameBoardEditor edit = new GameBoardEditor();

    void onGameModelSet(GoGameModel game) {
        model.initialize(game);
    }

    void onBoardSizeUpdate(double componentWidth, double componentHeight, GoGameModel game) {
        size.compute(componentWidth, componentHeight, game.getBoardWidth(), game.getBoardHeight(), view.getCoordinateLabelPosition());
    }

    void onGameUpdate(GoGameModel game) {
        model.update(game);
    }
}
