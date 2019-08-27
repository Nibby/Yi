package codes.nibby.yi.board;

import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Synchronises information between GameBoard display and Game object.
 */
public abstract class GameBoardController implements GameListener {

    private Game game;
    private GameBoard board;

    /**
     * Invoked by GameBoard class constructor.
     * @param game Current game object
     * @param board Game board object
     */
    public void initialize(Game game, GameBoard board) {
        this.board = board;
        this.game = game;
        this.game.addGameListener(this);
    }

    public void mouseMoved(int x, int y, int oldX, int oldY) {

    }

    public void mousePressed(int x, int y, int oldX, int oldY, MouseButton button) {

    }

    public void mouseReleased(int x, int y, int oldX, int oldY, MouseButton button) {

    }

    public void mouseDragged(int x, int y, int oldX, int oldY, MouseButton button) {

    }

    public void mouseScrolled(double notch) {

    }

    public void mouseEntered() {

    }

    public void mouseExited() {

    }

    public void keyPressed(KeyCode code) {

    }

    public void keyReleased(KeyCode code) {

    }

    public Game getGame() {
        return game;
    }

    public GameBoard getBoard() {
        return board;
    }
}
