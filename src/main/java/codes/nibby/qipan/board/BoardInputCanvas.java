package codes.nibby.qipan.board;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * The top-most layer of the board canvas stack.
 * This layer handles:
 * <ul>
 *     <li>Peripheral input, propagates them to GameBoard parent</li>
 *     <li>Rendering cursor indicators</li>
 *     <li>Rendering animated stones (where contiguous redraw is required)</li>
 * </ul>
 */
public class BoardInputCanvas extends Canvas {

    private GameBoard gameBoard;
    private GraphicsContext g;

    public BoardInputCanvas(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        g = getGraphicsContext2D();

        addEventHandler(MouseEvent.MOUSE_MOVED, this::mouseMoved);
        addEventHandler(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
        addEventHandler(MouseEvent.MOUSE_EXITED, this::mouseExited);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
        addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        addEventHandler(ScrollEvent.SCROLL, this::mouseScrolled);

        addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);
        addEventHandler(KeyEvent.KEY_RELEASED, this::keyReleased);

    }

    public void render() {

    }

    private void mouseMoved(MouseEvent evt) {

    }

    private void mouseEntered(MouseEvent evt) {

    }

    private void mouseExited(MouseEvent evt) {

    }

    private void mouseDragged(MouseEvent evt) {

    }

    private void mousePressed(MouseEvent evt) {

    }

    private void mouseReleased(MouseEvent evt) {

    }

    private void mouseScrolled(ScrollEvent evt) {

    }

    private void keyPressed(KeyEvent evt) {

    }

    private void keyReleased(KeyEvent evt) {

    }
}
