package yi.editor;

import yi.core.go.GameModel;
import yi.editor.components.EditorBoardArea;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionContext;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.framework.action.EditorBasicAction;

public class EditorUndoSystem {

    private final EditorAction undo;
    private final EditorAction redo;

    public EditorUndoSystem(EditorActionManager actionManager) {
        undo = new EditorBasicAction(actionManager, EditorTextResources.UNDO, this::requestUndo)
                    .setInMainMenu(EditorMainMenuType.EDIT, 0d)
                    .setAccelerator(EditorAcceleratorId.UNDO)
                    .setEnabled(false);

        redo = new EditorBasicAction(actionManager, EditorTextResources.REDO, this::requestRedo)
                .setInMainMenu(EditorMainMenuType.EDIT, 0.001d)
                .setAccelerator(EditorAcceleratorId.REDO)
                .setEnabled(false);
    }

    private void requestUndo(EditorActionContext context) {
        var frame = context.getEditorFrame();
        var board = frame.getBoardArea();
        boolean canUndoAgain = board.requestUndo();
        undo.setEnabled(canUndoAgain);
        redo.setEnabled(board.canRedo());
    }

    private void requestRedo(EditorActionContext context) {
        var frame = context.getEditorFrame();
        var board = frame.getBoardArea();
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
}
