package yi.component.board.edits;

import org.jetbrains.annotations.Nullable;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
import yi.core.go.Stone;
import yi.core.go.StoneColor;

public abstract class StoneEdit extends UndoableEdit {

    public static StoneEdit.Add addStone(@Nullable GameNode nodeToEdit, int x, int y, StoneColor color) {
        return new Add(nodeToEdit, x, y, color);
    }

    public static StoneEdit.Remove removeStone(@Nullable GameNode nodeToEdit, int x, int y) {
        return new Remove(nodeToEdit, x, y);
    }


    private final boolean createNewNode;
    protected GameNode nodeToEdit;

    private StoneEdit(@Nullable GameNode nodeToEdit) {
        this.createNewNode = nodeToEdit == null;
        this.nodeToEdit = nodeToEdit;
    }

    @Override
    protected boolean _rollbackEdit(GameModel gameModel) {
        boolean success = _rollbackStoneEdit(gameModel);

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
            nodeToEdit = gameModel.submitStoneEditNode();
        }

        return _performStoneEdit(gameModel);
    }

    @Override
    protected boolean canRollback() {
        return true;
    }

    protected abstract boolean _performStoneEdit(GameModel gameModel);
    protected abstract boolean _rollbackStoneEdit(GameModel gameModel);

    private static final class Add extends StoneEdit {

        private final Stone stoneEditToAdd;

        private Add(@Nullable GameNode nodeToEdit, int x, int y, StoneColor color) {
            super(nodeToEdit);

            if (color == StoneColor.NONE) {
                throw new IllegalArgumentException("Cannot add stone using StoneColor.NONE");
            }

            this.stoneEditToAdd = new Stone(x, y, color);
        }

        @Override
        protected boolean _performStoneEdit(GameModel gameModel) {
            assertStateCorrectBeforePerformEdit(gameModel);
            gameModel.addStoneEdit(nodeToEdit, stoneEditToAdd);
            return true;
        }

        @Override
        protected boolean _rollbackStoneEdit(GameModel gameModel) {
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
        private Stone stoneEditToRemove;

        private Remove(GameNode nodeToEdit, int x, int y) {
            super(nodeToEdit);

            this.x = x;
            this.y = y;
        }

        @Override
        protected boolean _performStoneEdit(GameModel gameModel) {
            stoneEditToRemove = getStoneEditToRemove(gameModel);
            gameModel.removeStoneEdit(nodeToEdit, stoneEditToRemove);
            return true;
        }

        @Override
        protected boolean _rollbackStoneEdit(GameModel gameModel) {
            if (stoneEditToRemove == null) {
                throw new IllegalStateException("No stone edit to remove");
            }
            gameModel.addStoneEdit(nodeToEdit, stoneEditToRemove);
            return true;
        }

        private Stone getStoneEditToRemove(GameModel gameModel) {
            var editHere = gameModel.getCurrentNode().getStoneEditCopyAt(x, y);
            if (editHere == null) {
                throw new IllegalStateException("No edit to remove at (" + x + ", " + y + ")");
            }
            return editHere;
        }
    }

}
