package yi.component.board.editmodes;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import yi.component.board.GameBoardManager;
import yi.component.board.edits.PlayMoveEdit;
import yi.models.go.*;

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

            playSounds(manager, playMoveEdit);
        } else {
            // TODO: This is only temporary.
            manager.getGameModel().submitPass();
        }
    }

    private void playSounds(GameBoardManager manager, PlayMoveEdit edit) {
        if (edit.getMoveValidationResult() == MoveValidationResult.OK) {
            var submittedNode = edit.getSubmittedNode();
            StoneColor moveColor = getStoneColor(manager.getGameModel(), edit);
            manager.audio.playMoveSound(moveColor);

            int captures = 0;
            if (submittedNode != null) {
                captures = submittedNode.getCapturesThisTurn().size();
            } else {
                GameNode currentNode = manager.getGameModel().getCurrentNode();
                captures = currentNode.getCapturesThisTurn().size();
            }

            if (captures > 0) {
                manager.audio.playCaptureSound(250, captures, moveColor);
            }
        }
    }

    private StoneColor getStoneColor(GameModel model, PlayMoveEdit edit) {
        var node = edit.getSubmittedNode() != null ? edit.getSubmittedNode() : model.getCurrentNode();
        var primaryMove = node.getPrimaryMove();
        StoneColor color;

        if (primaryMove != null) {
            color = primaryMove.getColor();
        } else if (node.getStoneEdits().size() == 1) {
            color = node.getStoneEdits().iterator().next().getColor();
        } else {
            int thisTurn = model.getCurrentMoveNumber();
            boolean isHandicapped = model.getInfo().getHandicapCount() > 0;
            color = model.getRules().getStoneColorForTurn(thisTurn, isHandicapped);
        }

        return color;
    }

    @Override
    public void onMouseRelease(MouseButton button, GameBoardManager manager, int cursorX, int cursorY) {

    }

    @Override
    public void onMouseDrag(MouseButton button, GameBoardManager manager, int gridX, int gridY) {
        // TODO: Feature - If the stone being dragged is the last move played, move the stone to the new position.
    }
}
