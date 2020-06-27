package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.weiqi.GoGameModel;

final class GameBoardManager {

    final GameBoardState state = new GameBoardState();
    final GameBoardSize sizes = new GameBoardSize();

    void onGameInitialize(GoGameModel game) {
        state.initialize(game.getBoardWidth(), game.getBoardHeight());
    }

    void onBoardSizeUpdate(double newBoardWidth, double newBoardHeight) {
        sizes.recalculate(newBoardWidth, newBoardHeight);
    }

    void onGameUpdate(GoGameModel game) {

    }
}
