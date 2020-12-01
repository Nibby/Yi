package yi.component.board.editmodes;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import org.jetbrains.annotations.Nullable;
import yi.component.board.GameBoardAudio;
import yi.component.board.GameBoardManager;
import yi.component.board.edits.PlayMoveEdit;
import yi.models.go.GameNode;
import yi.models.go.StoneColor;

import java.util.Optional;

final class PlayMoveEditMode extends AbstractEditMode {

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
    public void onMousePress(MouseButton button, GameBoardManager manager, int gridX, int gridY) {
        if (button == MouseButton.PRIMARY) {
            PlayMoveEdit playMoveEdit = PlayMoveEdit.forMove(gridX, gridY);
            manager.edit.recordAndApply(playMoveEdit, manager);

            playSounds(manager.audio, playMoveEdit.getSubmittedNode());
        } else {
            // TODO: This is only temporary.
            manager.getGameModel().submitPass();
        }
    }

    private void playSounds(GameBoardAudio audio, @Nullable GameNode submittedNode) {
        if (submittedNode != null && submittedNode.getPrimaryMove() != null) {
            StoneColor moveColor = submittedNode.getPrimaryMove().getColor();
            audio.playMoveSound(moveColor);

            int captures = submittedNode.getCaptures().size();
            if (captures > 0) {
                audio.playCaptureSound(250, captures, moveColor);
            }
        }
    }

    @Override
    public void onMouseRelease(MouseButton button, GameBoardManager manager, int cursorX, int cursorY) {

    }

    @Override
    public void onMouseDrag(MouseButton button, GameBoardManager manager, int gridX, int gridY) {
        // TODO: Feature - If the stone being dragged is the last move played, move the stone to the new position.
    }
}
