package codes.nibby.yi.gui.board;

import codes.nibby.yi.model.GoGame;

final class GameBoardManager {

    final GameBoardState state = new GameBoardState();
    final GameBoardSize boardSize = new GameBoardSize();

    void onGameInitialize(GoGame game) {
        state.initialize(game.getGridsHorizontal(), game.getGridsVertical());
    }

    void onBoardSizeUpdate(double newBoardWidth, double newBoardHeight) {
        boardSize.recalculate(newBoardWidth, newBoardHeight);
    }

    void onGameUpdate(GoGame game) {

    }
}
