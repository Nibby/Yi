package yi.component.board.edits;

import yi.core.go.GameModel;
import yi.core.go.GameNode;

import java.util.Objects;

abstract class UndoableEdit implements Undoable {

    @Override
    public boolean rollbackEdit(GameModel gameModel) {
        if (!canRollback())
            throw new IllegalStateException();

        return _rollbackEdit(gameModel);
    }

    @Override
    public boolean performEdit(GameModel gameModel) {
        return _performEdit(gameModel);
    }

    protected abstract boolean _rollbackEdit(GameModel gameModel);

    protected abstract boolean _performEdit(GameModel gameModel);

    protected abstract boolean canRollback();
}
