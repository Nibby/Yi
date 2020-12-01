package yi.component.board.edits;

import yi.models.go.GameModel;
import yi.models.go.GameNode;
import yi.models.go.MoveSubmitResult;
import yi.models.go.MoveValidationResult;

public final class PlayMoveEdit extends UndoableEdit {

    private enum MoveType {
        PLAY_MOVE,
        PASS
    }

    private final MoveType moveType;
    private int moveX;
    private int moveY;
    private GameNode parentOfSubmittedNode;
    private GameNode submittedNode;

    private PlayMoveEdit(int moveX, int moveY) {
        this(MoveType.PLAY_MOVE);
        this.moveX = moveX;
        this.moveY = moveY;
    }

    private PlayMoveEdit(MoveType moveType) {
        this.moveType = moveType;
    }

    @Override
    protected boolean rollbackEditNow(GameModel gameModel) {
        gameModel.removeNodeSubtree(submittedNode);
        gameModel.setCurrentNode(parentOfSubmittedNode); // Assuming parent is still on the tree
        return true;
    }

    @Override
    protected boolean performEditNow(GameModel gameModel) {
        if (submittedNode == null) {
            return submitMoveForFirstTimeEdit(gameModel);
        } else {
            return appendSubmittedNodeBackToGameTree(gameModel);
        }
    }

    private boolean appendSubmittedNodeBackToGameTree(GameModel gameModel) {
        gameModel.submitNode(parentOfSubmittedNode, submittedNode);
        return true;
    }

    private boolean submitMoveForFirstTimeEdit(GameModel gameModel) {
        var currentMoveBeforeNewMoveSubmission = gameModel.getCurrentNode();

        MoveSubmitResult moveSubmitResult = gameModel.submitMove(moveX, moveY);
        MoveValidationResult validationResult = moveSubmitResult.getValidationResult();

        if (validationResult != MoveValidationResult.OK) {
            return false;
        }

        if (!moveSubmitResult.isPlayed()) {
            throw new IllegalStateException("Move is not played internally, is it returning " +
                    "the result before being submitted to the game tree?");
        }

        submittedNode = moveSubmitResult.getMoveNode();
        parentOfSubmittedNode = currentMoveBeforeNewMoveSubmission;
        return true;
    }

    @Override
    protected boolean canRollback() {
        return submittedNode != null;
    }

    public final GameNode getSubmittedNode() {
        return submittedNode;
    }

    public static PlayMoveEdit forMove(int x, int y) {
        return new PlayMoveEdit(x, y);
    }

    public static PlayMoveEdit forPass() {
        return new PlayMoveEdit(MoveType.PASS);
    }
}
