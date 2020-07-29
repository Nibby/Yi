package yi.component.board.edits;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import yi.component.board.GameBoardManager;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;

import java.util.Optional;

final class PlayMoveEditMode implements EditMode {

    PlayMoveEditMode() { }

    @Override
    public void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY) {
        // TODO: Temporary. Probably should be disabled by default as the indicators are quite distracting.
        //       But can have this as a configurable preference.
//        if (manager.model.getCurrentGamePosition().getStoneColorAt(gridX, gridY) != GoStoneColor.NONE) {
//            // A stone already exists here, don't draw cursor.
//            return;
//        }
//
//        var nextTurnStoneColor = manager.model.getNextTurnStoneColor();
//        Color cursorColor = null;
//
//        if (nextTurnStoneColor == GoStoneColor.BLACK) {
//            cursorColor = Color.BLACK;
//        } else if (nextTurnStoneColor == GoStoneColor.WHITE) {
//            cursorColor = Color.WHITE;
//        }
//
//        if (cursorColor != null) {
//            double cursorSize = manager.size.getStoneSizeInPixels() / 2d;
//            double[] position = manager.size.getGridRenderPosition(gridX, gridY, cursorSize);
//
//            g.setFill(cursorColor);
//            g.fillRect(position[0], position[1], cursorSize, cursorSize);
//        }
    }

    @Override
    public Optional<Cursor> getMouseCursor() {
        return Optional.of(Cursor.HAND);
    }

    @Override
    public void onMousePress(GameBoardManager manager, int gridX, int gridY) {
        var playMoveEdit = new PlayMoveEdit(gridX, gridY);
        manager.edit.recordAndApply(playMoveEdit, manager);
        
    }

    @Override
    public void onKeyPress(GameBoardManager manager, KeyEvent e) {
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

    @Override
    public void onMouseDrag(GameBoardManager manager, int gridX, int gridY) {
        // TODO: Feature - If the stone being dragged is the last move played, move the stone to the new position.
    }
}
