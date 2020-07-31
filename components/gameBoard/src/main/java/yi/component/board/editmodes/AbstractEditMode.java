package yi.component.board.editmodes;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import yi.component.board.GameBoardManager;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import yi.component.utilities.GuiUtilities;

import java.util.Optional;

/**
 *
 * For a list of usable edit modes, see {@link EditMode}.
 */
public abstract class AbstractEditMode {

    public abstract void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY);

    public Optional<Cursor> getMouseCursor() {
        return Optional.of(Cursor.HAND);
    }

    public abstract void onMousePress(MouseButton button, GameBoardManager manager, int gridX, int gridY);

    public abstract void onMouseDrag(MouseButton button, GameBoardManager manager, int gridX, int gridY);

    public abstract void onMouseRelease(MouseButton button, GameBoardManager manager, int cursorX, int cursorY);

    public void onKeyPress(GameBoardManager manager, KeyEvent e) {
        handleTreeNavigation(manager, e);
    }

    protected void handleTreeNavigation(GameBoardManager manager, KeyEvent e) {
        if (e.getCode() == KeyCode.UP) {
            manager.model.toPreviousMove();
        } else if (e.getCode() == KeyCode.DOWN) {
            manager.model.toNextMove();
        } else if (e.getCode() == KeyCode.LEFT) {
            // TODO: Find the branching node and find a node of the same distance to root in its main branch.
        } else if (e.getCode() == KeyCode.RIGHT) {
            // TODO: Find the branching node and scan all children from left to right, selecting the first node of the same distance to root in its main branch.
        }
    }
}
