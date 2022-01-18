package codes.nibby.yi.app.components;

import codes.nibby.yi.models.GameModel;
import codes.nibby.yi.models.editor.GameModelUndoSystem;
import codes.nibby.yi.app.framework.AppText;
import codes.nibby.yi.app.framework.AppAccelerator;
import codes.nibby.yi.app.framework.action.AppAction;
import codes.nibby.yi.app.framework.action.AppActionContext;
import codes.nibby.yi.app.framework.action.AppActionManager;
import codes.nibby.yi.app.framework.action.AppBasicAction;

import java.util.Optional;

public final class AppUndoSupport implements AppComponent<Object> {

    private final AppAction undo;
    private final AppAction redo;

    public AppUndoSupport() {
        undo = new AppBasicAction(AppText.UNDO, this::requestUndo) {
            @Override
            public void refreshState(AppActionContext context) {
                super.refreshState(context);
                var window = context.getInvokerWindow();
                var model = window.getGameModel();
                var undoSystem = model.getEditor().getUndoSystem();
                undo.setEnabled(undoSystem.canUndo());
            }
        };
        undo.setInMenuBar(AppMainMenuType.EDIT, 0d);
        undo.setAccelerator(AppAccelerator.UNDO);
        undo.setEnabled(false);

        redo = new AppBasicAction(AppText.REDO, this::requestRedo) {
            @Override
            public void refreshState(AppActionContext context) {
                super.refreshState(context);
                var window = context.getInvokerWindow();
                var model = window.getGameModel();
                var undoSystem = model.getEditor().getUndoSystem();
                redo.setEnabled(undoSystem.canRedo());
            }
        };
        redo.setInMenuBar(AppMainMenuType.EDIT, 0.001d);
        redo.setAccelerator(AppAccelerator.REDO);
        redo.setEnabled(false);
    }

    private void requestUndo(AppActionContext context) {
        var window = context.getInvokerWindow();
        var model = window.getGameModel();
        var undoSystem = model.getEditor().getUndoSystem();
        if (undoSystem.canUndo()) {
            undoSystem.performUndo();
        }
        undo.setEnabled(undoSystem.canUndo());
        redo.setEnabled(undoSystem.canRedo());
    }

    private void requestRedo(AppActionContext context) {
        var window = context.getInvokerWindow();
        var model = window.getGameModel();
        var undoSystem = model.getEditor().getUndoSystem();
        if (undoSystem.canRedo()) {
            undoSystem.performRedo();
        }
        redo.setEnabled(undoSystem.canRedo());
        undo.setEnabled(undoSystem.canUndo());
    }

    public void setGameModel(GameModel newGameModel) {
        GameModelUndoSystem undoSystem = newGameModel.getEditor().getUndoSystem();
        newGameModel.onCurrentNodeChange().addListener(e -> refreshState(undoSystem));
        newGameModel.onNodeDataUpdate().addListener(e -> refreshState(undoSystem));
        newGameModel.onNodeAdd().addListener(e -> refreshState(undoSystem));
        newGameModel.onNodeRemove().addListener(e -> refreshState(undoSystem));
    }

    private void refreshState(GameModelUndoSystem undoSystem) {

        undo.setEnabled(undoSystem.canUndo());
        redo.setEnabled(undoSystem.canRedo());
    }

    @Override
    public AppAction[] getActions(AppActionManager actionManager) {
        return new AppAction[] { undo, redo };
    }

    @Override
    public Optional<Object> getComponent() {
        return Optional.empty();
    }
}
