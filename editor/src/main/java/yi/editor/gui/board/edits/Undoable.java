package yi.editor.gui.board.edits;

import yi.core.GoGameModel;

/**
 * Represents an edit made to the game model by the user.
 */
public interface Undoable {

    boolean rollbackEdit(GoGameModel gameModel);

    boolean performEdit(GoGameModel gameModel);

}
