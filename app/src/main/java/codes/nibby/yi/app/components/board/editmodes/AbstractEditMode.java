package codes.nibby.yi.app.components.board.editmodes;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import codes.nibby.yi.app.components.board.GameBoardManager;

import java.util.Optional;

/**
 * Edit modes restrict node data editing to a specific category, such as annotations
 * or helper stones.
 * <p/>
 * For a list of usable edit modes, see {@link EditMode}.
 */
public abstract class AbstractEditMode {

    /**
     * Draws the mouse-over cursor on the board intersections.
     *
     * @param g Draw graphics.
     * @param manager Board manager.
     * @param gridX Cursor logical X position.
     * @param gridY Cursor logical Y position.
     */
    public abstract void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY);

    /**
     *
     * @return Mouse cursor type.
     */
    public Optional<Cursor> getMouseCursor() {
        return Optional.of(Cursor.HAND);
    }

    /**
     * Called when mouse is pressed down on the board.
     *
     * @param button Mouse button that is pressed.
     * @param manager Board manager.
     * @param gridX Logical X position of the mouse at the time of event.
     * @param gridY Logical Y position of the mouse at the time of event.
     */
    public abstract void onMousePress(MouseButton button, GameBoardManager manager, int gridX, int gridY);

    /**
     * Called when mouse is dragging across the board.
     *
     * @param button Mouse button that is pressed.
     * @param manager Board manager.
     * @param gridX Logical X position of the mouse at the time of event.
     * @param gridY Logical Y position of the mouse at the time of event.
     */
    public abstract void onMouseDrag(MouseButton button, GameBoardManager manager, int gridX, int gridY);

    /**
     * Called when mouse a previously-pressed mouse button is released on the board.
     *
     * @param button Mouse button that is released.
     * @param manager Board manager.
     * @param cursorX Logical X position of the mouse at the time of event.
     * @param cursorY Logical Y position of the mouse at the time of event.
     */
    public abstract void onMouseRelease(MouseButton button, GameBoardManager manager, int cursorX, int cursorY);

    /**
     * Called when a key is pressed on the board.
     *
     * @param manager Board manager.
     * @param e FX Key event.
     */
    public void onKeyPress(GameBoardManager manager, KeyEvent e) {

    }

}
