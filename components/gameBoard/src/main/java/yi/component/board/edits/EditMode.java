package yi.component.board.edits;

import yi.component.board.GameBoardManager;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;

import java.util.Optional;

public interface EditMode {

    PlayMoveEditMode PLAY_MOVE = new PlayMoveEditMode();

    void onMousePress(GameBoardManager manager, int gridX, int gridY);

    void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY);

    Optional<Cursor> getMouseCursor();

}
