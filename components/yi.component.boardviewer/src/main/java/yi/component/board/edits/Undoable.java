package yi.component.board.edits;

import yi.models.go.GameModel;

/**
 * Represents an edit made to the game model by the user.
 */
public interface Undoable {

    boolean rollbackEdit(GameModel gameModel);

    boolean performEdit(GameModel gameModel);

}