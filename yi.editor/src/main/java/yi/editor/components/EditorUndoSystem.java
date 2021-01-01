package yi.editor.components;

import yi.core.go.GameModel;
import yi.editor.framework.EditorTextResources;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionContext;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.framework.action.EditorBasicAction;

import java.util.Optional;

public final class EditorUndoSystem implements EditorComponent<Object> {

    private final EditorAction undo;
    private final EditorAction redo;

    public EditorUndoSystem() {
        undo = new EditorBasicAction(EditorTextResources.UNDO, this::requestUndo) {
            @Override
            public void refreshState(EditorActionContext context) {
                super.refreshState(context);
                var window = context.getEditorWindow();
                var board = window.getBoardArea();
                undo.setEnabled(board.canUndo());
            }
        };
        undo.setInMainMenu(EditorMainMenuType.EDIT, 0d);
        undo.setAccelerator(EditorAcceleratorId.UNDO);
        undo.setEnabled(false);

        redo = new EditorBasicAction(EditorTextResources.REDO, this::requestRedo) {
            @Override
            public void refreshState(EditorActionContext context) {
                super.refreshState(context);
                var window = context.getEditorWindow();
                var board = window.getBoardArea();
                redo.setEnabled(board.canRedo());
            }
        };
        redo.setInMainMenu(EditorMainMenuType.EDIT, 0.001d);
        redo.setAccelerator(EditorAcceleratorId.REDO);
        redo.setEnabled(false);
    }

    private void requestUndo(EditorActionContext context) {
        var window = context.getEditorWindow();
        var board = window.getBoardArea();
        boolean canUndoAgain = board.requestUndo();
        undo.setEnabled(canUndoAgain);
        redo.setEnabled(board.canRedo());
    }

    private void requestRedo(EditorActionContext context) {
        var window = context.getEditorWindow();
        var board = window.getBoardArea();
        boolean canRedoAgain = board.requestRedo();
        redo.setEnabled(canRedoAgain);
        undo.setEnabled(board.canUndo());
    }

    public void setGameModel(GameModel newGameModel, EditorBoardArea boardArea) {
        refreshState(boardArea);

        newGameModel.onCurrentNodeChange().addListener(e -> refreshState(boardArea));
        newGameModel.onNodeDataUpdate().addListener(e -> refreshState(boardArea));
        newGameModel.onNodeAdd().addListener(e -> refreshState(boardArea));
        newGameModel.onNodeRemove().addListener(e -> refreshState(boardArea));
    }

    private void refreshState(EditorBoardArea boardArea) {
        undo.setEnabled(boardArea.canUndo());
        redo.setEnabled(boardArea.canRedo());
    }

    @Override
    public EditorAction[] getActions(EditorActionManager actionManager) {
        return new EditorAction[] { undo, redo };
    }

    @Override
    public Optional<Object> getComponent() {
        return Optional.empty();
    }
}
