package codes.nibby.yi.editor.gui.board.edit;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an edit made to the game model by the user.
 */
public interface Undoable {

    @NotNull
    Runnable rollbackEdit();

    @NotNull
    Runnable performEdit();

}
