package yi.component.board.edits;

import yi.models.go.GameModel;
import yi.models.go.GameNode;

import java.util.Objects;

public final class RemoveNodeEdit extends UndoableEdit {

    private final GameNode parentOfNodeToRemove;
    private final GameNode nodeToRemove;

    public RemoveNodeEdit(GameNode nodeToRemove) {
        if (nodeToRemove.isRoot()) {
            throw new IllegalArgumentException("Cannot remove root node");
        }

        this.nodeToRemove = Objects.requireNonNull(nodeToRemove);
        this.parentOfNodeToRemove =  Objects.requireNonNull(nodeToRemove.getParent());
    }

    @Override
    protected boolean rollbackEditNow(GameModel gameModel) {
        if (!parentOfNodeToRemove.isContinuationOf(gameModel.getRootNode())) {
            throw new IllegalStateException("Attempting to re-do but parent node is no " +
                    "longer part of the game tree");
        }

        gameModel.submitNode(parentOfNodeToRemove, nodeToRemove);
        return true;
    }

    @Override
    protected boolean performEditNow(GameModel gameModel) {
        gameModel.removeNodeSubtree(nodeToRemove);
        return true;
    }

    @Override
    protected boolean canRollback() {
        return false;
    }
}