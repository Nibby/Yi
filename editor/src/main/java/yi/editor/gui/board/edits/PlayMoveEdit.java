package yi.editor.gui.board.edits;

import yi.core.MoveNode;
import yi.core.*;

public final class PlayMoveEdit extends UndoableEdit {

    private final int moveX;
    private final int moveY;
    private MoveNode<GameStateUpdate> submittedNode;

    public PlayMoveEdit(int moveX, int moveY) {
        this.moveX = moveX;
        this.moveY = moveY;
    }

    @Override
    protected boolean _rollbackEdit(GoGameModel gameModel) {
        // TODO: Implement later
        return false;
    }

    @Override
    protected boolean _performEdit(GoGameModel gameModel) {
        MoveSubmitResult moveSubmitResult = gameModel.playMove(moveX, moveY);
        MoveValidationResult validationResult = moveSubmitResult.getValidationResult();

        if (moveSubmitResult.getValidationResult() != MoveValidationResult.OK)
            throw new SubmitMoveException(validationResult, "Illegal move: " + validationResult.toString());

        assert moveSubmitResult.getPlayed() : "Move is not played internally, is it returning the result before being submitted to the game tree?";

        submittedNode = moveSubmitResult.getMoveNode();

        return true;
    }

    @Override
    protected boolean canRollback() {
        return submittedNode != null;
    }
}
