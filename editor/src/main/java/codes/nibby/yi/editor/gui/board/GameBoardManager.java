package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.editor.model.GoGame;

final class GameBoardManager {

    final GameBoardState state = new GameBoardState();
    final GameBoardSize sizes = new GameBoardSize();

    void onGameInitialize(GoGame game) {
        state.initialize(game.getGridsHorizontal(), game.getGridsVertical());
    }

    void onBoardSizeUpdate(double newBoardWidth, double newBoardHeight) {
        sizes.recalculate(newBoardWidth, newBoardHeight);
    }

    void onGameUpdate(GoGame game) {

    }
}
