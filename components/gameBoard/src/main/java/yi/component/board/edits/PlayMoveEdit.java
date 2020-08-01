package yi.component.board.edits;

import yi.core.go.GameNode;
import yi.core.go.*;

public final class PlayMoveEdit extends UndoableEdit {

    private final int moveX;
    private final int moveY;
    private GameNode parentOfSubmittedNode;
    private GameNode submittedNode;

    public PlayMoveEdit(int moveX, int moveY) {
        this.moveX = moveX;
        this.moveY = moveY;
    }

    @Override
    protected boolean _rollbackEdit(GameModel gameModel) {
        gameModel.removeNodeSubtree(submittedNode);
        gameModel.setCurrentMove(parentOfSubmittedNode); // Assuming parent is still on the tree
        return true;
    }

    @Override
    protected boolean _performEdit(GameModel gameModel) {
        if (submittedNode == null) {
            return submitMoveForFirstTimeEdit(gameModel);
        } else {
            return appendSubmittedNodeBackToGameTree(gameModel);
        }
    }

    private boolean appendSubmittedNodeBackToGameTree(GameModel gameModel) {
        gameModel.submitMove(parentOfSubmittedNode, submittedNode);
        return true;
    }

    private boolean submitMoveForFirstTimeEdit(GameModel gameModel) {
        var currentMoveBeforeNewMoveSubmission = gameModel.getCurrentMove();

        MoveSubmitResult moveSubmitResult = gameModel.playMove(moveX, moveY);
        MoveValidationResult validationResult = moveSubmitResult.getValidationResult();

        if (validationResult != MoveValidationResult.OK) {
            return false;
        }

        if (!moveSubmitResult.isPlayed()) {
            throw new IllegalStateException("Move is not played internally, is it returning the result before being submitted to the game tree?");
        }

        submittedNode = moveSubmitResult.getMoveNode();
        parentOfSubmittedNode = currentMoveBeforeNewMoveSubmission;
        return true;
    }

    @Override
    protected boolean canRollback() {
        return submittedNode != null;
    }
}
