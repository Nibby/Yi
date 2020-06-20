package codes.nibby.yi.gui.board;

final class GameBoardManager {

    final GameBoardSize boardSize = new GameBoardSize();

    void onBoardSizeUpdate(double newBoardWidth, double newBoardHeight) {
        boardSize.recalculate(newBoardWidth, newBoardHeight);
    }
}
