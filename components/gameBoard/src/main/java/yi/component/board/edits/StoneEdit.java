package yi.component.board.edits;

import org.jetbrains.annotations.Nullable;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
import yi.core.go.Stone;
import yi.core.go.StoneColor;

import java.util.Objects;

/**
 * Helper stone edits are made to the current node if it is of delta type
 * {@link yi.core.go.GameNodeType#STONE_EDIT}. Otherwise, a new game node of this type
 * will be created by this edit.
 */
public final class StoneEdit extends UndoableEdit {

    private boolean thisEditCreatedNewNode = false;
    private GameNode nodeToEdit;
    private final Stone editToPerform;
    private final Stone editRemoved = null;

    public StoneEdit(@Nullable GameNode nodeToEdit, Stone stone) {
        this.editToPerform = Objects.requireNonNull(stone);
        this.nodeToEdit = nodeToEdit;
    }

    @Override
    protected boolean _rollbackEdit(GameModel gameModel) {
        if (nodeToEdit == null) {
            throw new IllegalStateException("Attempting to undo when nodeToEdit is null.");
        }

        gameModel.removeStoneEdit(nodeToEdit, editToPerform);

        // If this edit is the first edit that created the edit node, remove it
        if (thisEditCreatedNewNode) {
            if (!nodeToEdit.isLastMove()) {
                throw new IllegalStateException("Node is not leaf despite this edit authored it.");
            }
            gameModel.removeNodeSubtree(nodeToEdit);
            thisEditCreatedNewNode = true;
        }
        return false;
    }

    @Override
    protected boolean _performEdit(GameModel gameModel) {
        if (nodeToEdit == null) {
            nodeToEdit = gameModel.submitStoneEditNode();
            thisEditCreatedNewNode = true;
        }
        if (editToPerform.getColor() == StoneColor.NONE) {
            // TODO: Need to remove the old edit and use the new one if an old exists
            //       otherwise use the new.
            gameModel.removeStoneEdit(nodeToEdit, editToPerform);
        } else {
            gameModel.addStoneEdit(nodeToEdit, editToPerform);
        }

        return false;
    }

    @Override
    protected boolean canRollback() {
        return false;
    }

    public boolean createdNodeToEdit() {
        return thisEditCreatedNewNode;
    }


}
