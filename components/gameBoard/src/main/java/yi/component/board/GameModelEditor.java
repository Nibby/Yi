package yi.component.board;

import yi.component.board.editmodes.AbstractEditMode;
import yi.component.board.editmodes.EditMode;
import yi.component.board.edits.AnnotationEdit;
import yi.component.board.edits.Undoable;
import yi.core.go.GameModel;

import java.util.Objects;
import java.util.Stack;

/**
 * Manages all the edits made to the game model, providing support for undo and redo actions. The editing model requires
 * all changes to the game model to be submitted through this handler. Therefore, this class should be the only place that
 * directly interacts with {@link GameModel} state.
 */
public final class GameModelEditor {

    private final Stack<Undoable> undoHistory = new Stack<>();
    private int maxHistorySize = 30;
    private int positionInHistory = 0;

    private boolean editable = false;
    private AbstractEditMode editMode = EditMode.playMove();

    GameModelEditor() { }

    public void recordAndApply(Undoable undoable, GameBoardManager manager) {
        var gameModel = manager.getGameModel();

        boolean successful = undoable.performEdit(gameModel);

        if (successful) {
            // TODO: Making one exception for AnnotationEdit to aggregate fast edits under one undoable action.
            //       If this becomes a common pattern, abstract this mechanism away.
            if (!undoHistory.isEmpty() && undoable instanceof AnnotationEdit && undoHistory.peek() instanceof AnnotationEdit) {
                var topOfHistoryStack = (AnnotationEdit) undoHistory.peek();
                var annotationEdit = ((AnnotationEdit) undoable);

                if (topOfHistoryStack.isSameSession(annotationEdit)) {
                    mergeWithMostRecentAnnotationEditInHistory(annotationEdit);
                } else {
                    pushChangeToUndoHistory(undoable);
                }
            } else {
                pushChangeToUndoHistory(undoable);
            }
        }
    }

    private void mergeWithMostRecentAnnotationEditInHistory(AnnotationEdit undoable) {
        var firstItemToUndo = undoHistory.peek();

        if (!(firstItemToUndo instanceof AnnotationEdit)) {
            throw new IllegalStateException("First item is not AnnotationEdit so cannot be merged.");
        }

        ((AnnotationEdit) firstItemToUndo).merge(undoable);
    }

    private void pushChangeToUndoHistory(Undoable undoable) {
        if (positionInHistory < undoHistory.size() - 1) {
            // Discard the existing edit history
            int originalSize = undoHistory.size();
            for (int i = positionInHistory + 1; i < originalSize; ++i) {
                undoHistory.pop();
            }
        }

        undoHistory.push(undoable);
        positionInHistory = undoHistory.indexOf(undoable);

        pruneHistorySize();
    }

    private void pruneHistorySize() {
        while (undoHistory.size() > maxHistorySize) {
            undoHistory.remove(0);
            --positionInHistory;
        }
    }

    /**
     * Rollback the last undoable operation that was applied to the game model through {@link #recordAndApply(Undoable, GameBoardManager)} while
     * keeping the editing history. This means the action that was undone can be re-done.
     */
    public void performUndo(GameBoardManager manager) {
        var gameModel = manager.getGameModel();

        if (!canUndo()) {
            throw new IllegalStateException("Current position in history does not support undo." +
                    " positionInHistory: " + positionInHistory + ", editHistorySize: " + undoHistory.size());
        }
        if (positionInHistory >= 0) {
            Undoable currentEdit = undoHistory.get(positionInHistory);
            boolean successful = currentEdit.rollbackEdit(gameModel);

            if (successful) {
                positionInHistory--;
            }
        }
    }

    /**
     * Re-perform the edit in the current position in history.
     */
    public void performRedo(GameBoardManager manager) {
        var gameModel = manager.getGameModel();

        if (!canRedo()) {
            throw new IllegalStateException("Current position in history does not support redo." +
                    " positionInHistory: " + positionInHistory + ", editHistorySize: " + undoHistory.size());
        }

        Undoable currentEdit = undoHistory.get(positionInHistory + 1);
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
        return undoHistory.size() > 0 && positionInHistory >= 0;
    }

    /**
     *
     * @return true if {@link #performRedo(GameBoardManager)} can be performed.
     */
    public boolean canRedo() {
        int nextPosition = positionInHistory + 1;
        return nextPosition < undoHistory.size();
    }

    /**
     * Completely erases the editing history. This is a destructive operation!
     */
    public void clearEditHistory() {
        undoHistory.clear();
        positionInHistory = undoHistory.size();
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public AbstractEditMode getEditMode() {
        return editMode;
    }

    public void setEditMode(AbstractEditMode editMode) {
        this.editMode = Objects.requireNonNull(editMode, "Edit mode cannot be null. To disable editing, use setEditable(false)");
    }

    public int getMaxHistorySize() {
        return maxHistorySize;
    }

    public void setMaxHistorySize(int maxHistorySize) {
        if (maxHistorySize < 0) {
            throw new IllegalArgumentException("Maximum history size must be a positive integer");
        }

        this.maxHistorySize = maxHistorySize;
        pruneHistorySize();
    }

    public int getCurrentHistorySize() {
        return undoHistory.size();
    }

    /**
     *
     * @return Current position in the undo stack history.
     */
    public int getCurrentPositionInHistory() {
        return positionInHistory;
    }

    /**
     * Retrieves the {@link Undoable} edit item in the undo stack at the given position.
     *
     * @param position Position index of the item.
     * @return Item in undo history at the given position index.
     */
    public Undoable getHistoryItem(int position) {
        if (position < 0 || position > undoHistory.size()) {
            throw new IllegalArgumentException("Position out of bounds: " + position + ", must be within 0 - " + (undoHistory.size() - 1));
        }

        return undoHistory.get(position);
    }
}
