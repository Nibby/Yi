package codes.nibby.yi.editor.gui.board.edits;

import codes.nibby.yi.common.MoveNode;
import codes.nibby.yi.go.*;

public final class PlayMoveEdit extends UndoableEdit {

    private final int moveX;
    private final int moveY;
    private MoveNode<GameStateUpdate> submittedNode;

    public PlayMoveEdit(int moveX, int moveY) {
        this.moveX = moveX;
        this.moveY = moveY;
    }

    @Override
    public boolean rollbackEdit(GoGameModel gameModel) {
        if (submittedNode == null)
            throw new IllegalStateException("Edit was never applied to the game model");

        // TODO: Implement later
        return false;
    }

    @Override
    public boolean performEdit(GoGameModel gameModel) {
        MoveSubmitResult moveSubmitResult = gameModel.playMove(moveX, moveY);
        MoveValidationResult validationResult = moveSubmitResult.getValidationResult();

        if (moveSubmitResult.getValidationResult() != MoveValidationResult.OK)
            throw new SubmitMoveException(validationResult, "Illegal move: " + validationResult.toString());

        assert moveSubmitResult.getPlayed() : "Move is not played internally, is it returning the result before being submitted to the game tree?";

        submittedNode = moveSubmitResult.getMoveNode();

        return true;
    }
}
