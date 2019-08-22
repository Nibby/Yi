package codes.nibby.qipan.game;

/**
 * The core representation for a Go 'game'. This class contains all the
 * information to generate a SGF file.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class GoGame {

    private int boardWidth, boardHeight;

    public GoGame() {

    }

    public void setBoardSize(int width, int height) {
        this.boardWidth = width;
        this.boardHeight = height;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }
}
