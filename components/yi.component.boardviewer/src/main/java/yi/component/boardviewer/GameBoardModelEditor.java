package yi.component.boardviewer;

import yi.core.go.GameModel;
import yi.core.go.editor.edit.GameModelEdit;

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
