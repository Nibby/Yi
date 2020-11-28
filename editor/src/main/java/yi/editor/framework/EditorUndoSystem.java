package yi.editor.framework;

public interface EditorUndoSystem {

    boolean canUndo();

    void requestUndo();

    boolean canRedo();

    void requestRedo();

}
