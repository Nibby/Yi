package yi.component.boardviewer.edits;

import yi.core.go.GameModel;

/**
 * Represents an edit made to the game model by the user.
 */
public interface Undoable {

    boolean rollbackEdit(GameModel gameModel);

    boolean performEdit(GameModel gameModel);

}
