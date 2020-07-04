package codes.nibby.yi.editor.gui.board.edit;

import org.jetbrains.annotations.NotNull;

public final class PlayMoveEdit extends UndoableEdit {



    @Override
    public @NotNull Runnable rollbackEdit() {
        return null;
    }

    @Override
    public @NotNull Runnable performEdit() {
        return null;
    }

}
