package yi.component.board.editmodes;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import yi.component.board.GameBoardManager;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;

import java.util.Optional;

public interface EditMode {

    PlayMoveEditMode PLAY_MOVE = new PlayMoveEditMode();

    void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY);

    default Optional<Cursor> getMouseCursor() {
        return Optional.of(Cursor.HAND);
    }

    void onMousePress(MouseButton button, GameBoardManager manager, int gridX, int gridY);

    void onMouseDrag(MouseButton button, GameBoardManager manager, int gridX, int gridY);

    void onKeyPress(GameBoardManager manager, KeyEvent e);

    void onMouseRelease(MouseButton button, GameBoardManager manager, int cursorX, int cursorY);
}
