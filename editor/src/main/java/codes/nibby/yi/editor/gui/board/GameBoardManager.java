package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.go.GoGameModel;

final class GameBoardManager {

    final GameBoardState state = new GameBoardState();
    final GameBoardSize sizes = new GameBoardSize();

    void onGameInitialize(GoGameModel game) {
        state.initialize(game.getBoardWidth(), game.getBoardHeight());
    }

    void onBoardSizeUpdate(double newBoardWidth, double newBoardHeight, GoGameModel game) {
        sizes.recalculate(newBoardWidth, newBoardHeight, game.getBoardWidth(), game.getBoardHeight());
    }

    void onGameUpdate(GoGameModel game) {

    }
}
