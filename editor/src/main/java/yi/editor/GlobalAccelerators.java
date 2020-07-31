package yi.editor;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import yi.component.utilities.SystemUtilities;

import static javafx.scene.input.KeyCombination.ModifierValue.DOWN;
import static javafx.scene.input.KeyCombination.ModifierValue.UP;

public final class GlobalAccelerators {

    private UndoSystemHandler undoSystem;

    void installFor(Scene scene) {
        installUndoSystemAccelerators(scene);
    }

    private void installUndoSystemAccelerators(Scene scene) {
        KeyCodeCombination undo;
        KeyCodeCombination redo;

        if (SystemUtilities.isMac()) {
            undo = new KeyCodeCombination(KeyCode.Z, UP, UP, UP, DOWN, UP);
            redo = new KeyCodeCombination(KeyCode.Z, DOWN, UP, UP, DOWN, UP);
        } else if (SystemUtilities.isWindows()) {
            undo = new KeyCodeCombination(KeyCode.Z, UP, DOWN, UP, UP, UP);
            redo = new KeyCodeCombination(KeyCode.Y, UP, DOWN, UP, UP, UP);
        } else {
            undo = new KeyCodeCombination(KeyCode.Z, UP, DOWN, UP, UP, UP);
            redo = new KeyCodeCombination(KeyCode.Z, DOWN, DOWN, UP, UP, UP);
        }

        Runnable undoAction = () -> { if (undoSystem != null) undoSystem.requestUndo(); };
        Runnable redoAction = () -> { if (undoSystem != null) undoSystem.requestRedo(); };

        scene.getAccelerators().put(undo, undoAction);
        scene.getAccelerators().put(redo, redoAction);
    }

    public void setUndoSystemHandler(UndoSystemHandler undoSystem) {
        this.undoSystem = undoSystem;
    }

    interface UndoSystemHandler {
        void requestUndo();
        void requestRedo();
    }
}
