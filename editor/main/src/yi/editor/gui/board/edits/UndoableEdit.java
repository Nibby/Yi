package yi.editor.gui.board.edits;

import yi.core.go.GoGameModel;

public abstract class UndoableEdit implements Undoable {

    @Override
    public boolean rollbackEdit(GoGameModel gameModel) {
        if (!canRollback())
            throw new IllegalStateException();

        return _rollbackEdit(gameModel);
    }

    @Override
    public boolean performEdit(GoGameModel gameModel) {
        return _performEdit(gameModel);
    }

    protected abstract boolean _rollbackEdit(GoGameModel gameModel);

    protected abstract boolean _performEdit(GoGameModel gameModel);

    protected abstract boolean canRollback();
}
