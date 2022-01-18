package codes.nibby.yi.app.components.board;

import codes.nibby.yi.models.GameModel;
import codes.nibby.yi.models.editor.edit.GameModelEdit;

// TODO: Possibly not needed
public final class GameBoardModelEditor {

    private final GameBoardManager manager;

    public GameBoardModelEditor(GameBoardManager manager) {
        this.manager = manager;
    }

    public void submit(GameModelEdit edit) {
        GameModel model = manager.getGameModel();
        var editor = model.getEditor();
        editor.recordAndApplyUndoable(edit);
    }

    public boolean isEditable() {
        return manager.getGameModel().getEditor().isEditable();
    }
}
