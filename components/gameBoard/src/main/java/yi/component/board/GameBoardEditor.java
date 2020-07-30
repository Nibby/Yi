package yi.component.board;

import yi.component.board.editmodes.EditMode;
import yi.core.go.GameModel;
import yi.component.board.edits.Undoable;

import java.util.Stack;

/**
 * Manages all the edits made to the game model, providing support for undo and redo actions. The editing model requires
 * all changes to the game model to be submitted through this handler.
 */
public final class GameBoardEditor {

    private final Stack<Undoable> editHistory = new Stack<>();
    private int positionInHistory = 0;

    private boolean editable = false;
    private EditMode editMode = EditMode.PLAY_MOVE;

    GameBoardEditor() { }

    public void recordAndApply(Undoable undoable, GameBoardManager manager) {
        var gameModel = getGameModelOrCrash(manager);

        boolean successful = undoable.performEdit(gameModel);

        if (successful) {
            if (positionInHistory < editHistory.size() - 1) {
                // Discard the existing edit history
                for (int i = positionInHistory; i < editHistory.size(); ++i) {
                    editHistory.remove(editHistory.size() - 1);
                }
            }

            editHistory.push(undoable);
            positionInHistory = editHistory.size() - 1;
        }
    }

    /**
     * Rollback the last undoable operation that was applied to the game model through {@link #recordAndApply(Undoable, GameBoardManager)} while
     * keeping the editing history. This means the action that was undone can be re-done.
     */
    public void performUndo(GameBoardManager manager) {
        var gameModel = getGameModelOrCrash(manager);

        if (!canUndo())
            throw new IllegalStateException("Current position in history does not support undo." +
                    " positionInHistory: " + positionInHistory + ", editHistorySize: " + editHistory.size());

        Undoable currentEdit = editHistory.get(positionInHistory);
        boolean successful = currentEdit.rollbackEdit(gameModel);

        if (successful) {
            positionInHistory--;
        }
    }

    /**
     * Re-perform the edit in the current position in history.
     */
    public void performRedo(GameBoardManager manager) {
        var gameModel = getGameModelOrCrash(manager);

        if (!canRedo())
            throw new IllegalStateException("Current position in history does not support redo." +
                    " positionInHistory: " + positionInHistory + ", editHistorySize: " + editHistory.size());

        Undoable currentEdit = editHistory.get(positionInHistory);
        boolean successful = currentEdit.performEdit(gameModel);

        if (successful) {
            positionInHistory++;
        }
    }

    /**
     *
     * @return true if {@link #performUndo(GameBoardManager)} can be performed.
     */
    public boolean canUndo() {
        int previousPosition = positionInHistory - 1;
        return previousPosition >= 0;
    }

    /**
     *
     * @return true if {@link #performRedo(GameBoardManager)} can be performed.
     */
    public boolean canRedo() {
        int nextPosition = positionInHistory + 1;
        return nextPosition <= editHistory.size();
    }

    /**
     * Completely erases the editing history. This is a destructive operation!
     */
    public void clearEditHistory() {
        editHistory.clear();
        positionInHistory = editHistory.size();
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public EditMode getEditMode() {
        return editMode;
    }

    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

    private GameModel getGameModelOrCrash(GameBoardManager manager) {
        var gameModel = manager.model.getGameModel();

        if (gameModel == null)
            throw new IllegalStateException("Game model is unset");

        return gameModel;
    }
}
