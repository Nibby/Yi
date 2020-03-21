package codes.nibby.yi.board;

import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.GameNode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * Handles user input from game board. It is important to invoke {@link #setBoard(GameBoard)} once
 * the controller has been added to a game board.
 */
public abstract class GameBoardController implements GameListener {

    private WeakReference<GameBoard> board;

    void setBoard(GameBoard board) {
        this.board = new WeakReference<>(board);
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

    protected Game getGame() {
        return getBoard().getGame();
    }

    @NotNull
    public GameBoard getBoard() {
        GameBoard gameBoard = board.get();
        assert gameBoard != null : "gameBoard is null! Make sure to call .setBoard() when the controller is added to the board.";
        return gameBoard;
    }
}
