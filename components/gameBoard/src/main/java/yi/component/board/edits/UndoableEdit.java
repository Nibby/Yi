package yi.component.board.edits;

import yi.core.go.GameModel;

public abstract class UndoableEdit implements Undoable {

    @Override
    public boolean rollbackEdit(GameModel gameModel) {
        if (!canRollback())
            throw new IllegalStateException();

        return rollbackEditNow(gameModel);
    }

    @Override
    public boolean performEdit(GameModel gameModel) {
        return performEditNow(gameModel);
    }

    protected abstract boolean rollbackEditNow(GameModel gameModel);

    protected abstract boolean performEditNow(GameModel gameModel);

    protected abstract boolean canRollback();
}
