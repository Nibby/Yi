package yi.models.yi.editor.gui.board.edits;

import yi.models.yi.editor.gui.board.GameBoardManager;
import yi.go.GoStoneColor;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Optional;

final class PlayMoveEditMode implements EditMode {

    PlayMoveEditMode() { }

    @Override
    public void onMousePress(GameBoardManager manager, int gridX, int gridY) {
        var playMoveEdit = new PlayMoveEdit(gridX, gridY);
        manager.edit.recordAndApply(playMoveEdit, manager);
    }

    @Override
    public void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY) {
        if (manager.model.getCurrentGamePosition().getStoneColorAt(gridX, gridY) != GoStoneColor.NONE) {
            // A stone already exists here, don't draw cursor.
            return;
        }

        var nextTurnStoneColor = manager.model.getNextTurnStoneColor();
        Color cursorColor = null;

        if (nextTurnStoneColor == GoStoneColor.BLACK) {
            cursorColor = Color.BLACK;
        } else if (nextTurnStoneColor == GoStoneColor.WHITE) {
            cursorColor = Color.WHITE;
        }

        if (cursorColor != null) {
            double cursorSize = manager.size.getStoneSizeInPixels() / 2d;
            double[] position = manager.size.getGridRenderPosition(gridX, gridY, cursorSize);

            g.setFill(cursorColor);
            g.fillRect(position[0], position[1], cursorSize, cursorSize);
        }
    }

    @Override
    public Optional<Cursor> getMouseCursor() {
        return Optional.of(Cursor.HAND);
    }

}
