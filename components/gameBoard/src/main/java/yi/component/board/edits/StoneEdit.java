package yi.component.board.edits;

import org.jetbrains.annotations.Nullable;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
import yi.core.go.Stone;
import yi.core.go.StoneColor;

import java.util.Optional;

public abstract class StoneEdit extends UndoableEdit {

    public static StoneEdit.Add add(@Nullable GameNode nodeToEdit, int x, int y, StoneColor color) {
        return new Add(nodeToEdit, x, y, color);
    }

    public static StoneEdit.Remove remove(@Nullable GameNode nodeToEdit, int x, int y) {
        return new Remove(nodeToEdit, x, y);
    }


    private final boolean createNewNode;
    private GameNode nodeToEdit;

    private StoneEdit(@Nullable GameNode nodeToEdit) {
        this.createNewNode = nodeToEdit == null;
        this.nodeToEdit = nodeToEdit;
    }

    @Override
    protected boolean _rollbackEdit(GameModel gameModel) {
        if (nodeToEdit == null) {
            throw new IllegalStateException("Attempting to undo before the edit action has been performed once.");
        }

        boolean success = _rollbackStoneEdit(gameModel, nodeToEdit);

        if (!success) {
            return false;
        }

        if (createNewNode) {
            gameModel.removeNodeSubtree(nodeToEdit);
        }

        return true;
    }

    @Override
    protected boolean _performEdit(GameModel gameModel) {
        if (createNewNode) {
            if (nodeToEdit == null) {
                // First time submitting node
                nodeToEdit = gameModel.submitStoneEditNode();
            } else {
                // Subsequent re-do actions. Have to re-use this node because subsequent edits also refer to this node
                // when undo/redoing their changes.
                gameModel.submitNode(nodeToEdit);
            }
        }

        return _performStoneEdit(gameModel, nodeToEdit);
    }

    @Override
    protected boolean canRollback() {
        return true;
    }

    protected abstract boolean _performStoneEdit(GameModel gameModel, GameNode nodeToEdit);
    protected abstract boolean _rollbackStoneEdit(GameModel gameModel, GameNode nodeToEdit);

    private static final class Add extends StoneEdit {

        private final Stone stoneEditToAdd;

        private Add(@Nullable GameNode nodeToEdit, int x, int y, StoneColor color) {
            super(nodeToEdit);
            this.stoneEditToAdd = new Stone(x, y, color);
        }

        @Override
        protected boolean _performStoneEdit(GameModel gameModel, GameNode nodeToEdit) {
            assertStateCorrectBeforePerformEdit(gameModel);
            gameModel.addStoneEdit(nodeToEdit, stoneEditToAdd);
            return true;
        }

        @Override
        protected boolean _rollbackStoneEdit(GameModel gameModel, GameNode nodeToEdit) {
            assertStateCorrectBeforeRollbackEdit(gameModel);
            gameModel.removeStoneEdit(nodeToEdit, stoneEditToAdd);
            return true;
        }

        private void assertStateCorrectBeforePerformEdit(GameModel gameModel) {
            int x = stoneEditToAdd.getX();
            int y = stoneEditToAdd.getY();
            var editHere = gameModel.getCurrentNode().getStoneEditCopyAt(x, y);
            if (editHere != null) {
                throw new IllegalStateException("Attempting to perform stone edit but a stone already exists at (" + x + ", " + y + ")");
            }
        }

        private void assertStateCorrectBeforeRollbackEdit(GameModel gameModel) {
            int x = stoneEditToAdd.getX();
            int y = stoneEditToAdd.getY();
            var editHere = gameModel.getCurrentNode().getStoneEditCopyAt(x, y);
            if (editHere == null) {
                throw new IllegalStateException("Attempting to un-add stone edit but no stone exist at (" + x + ", " + y + ")");
            }
        }
    }

    private static final class Remove extends StoneEdit {

        private final int x;
        private final int y;
        private Stone stoneEdit;

        private Remove(GameNode nodeToEdit, int x, int y) {
            super(nodeToEdit);

            this.x = x;
            this.y = y;
        }

        @Override
        protected boolean _performStoneEdit(GameModel gameModel, GameNode nodeToEdit) {
            // The stone we wish to remove can either exist as a stone edit in this node delta
            // or in one of the parents... So there's no one size fit all solution.
            //
            // If there is an existing stone edit at this position, delete it.
            // Otherwise, create a new stone edit which sets the intersection to NONE
            var existingEditOnThisNode = getStoneEditToRemove(gameModel);

            if (existingEditOnThisNode.isPresent()) {
                stoneEdit = existingEditOnThisNode.get();
                gameModel.removeStoneEdit(nodeToEdit, stoneEdit);
            } else throw new IllegalStateException("No stone was created at (" + x + ", " + y + ") on this node.");

            return true;
        }

        @Override
        protected boolean _rollbackStoneEdit(GameModel gameModel, GameNode nodeToEdit) {
            if (stoneEdit == null) {
                throw new IllegalStateException("No stone edit to remove.");
            }
            gameModel.addStoneEdit(nodeToEdit, stoneEdit);
            return true;
        }

        private Optional<Stone> getStoneEditToRemove(GameModel gameModel) {
            return Optional.ofNullable(gameModel.getCurrentNode().getStoneEditCopyAt(x, y));
        }
    }

}
