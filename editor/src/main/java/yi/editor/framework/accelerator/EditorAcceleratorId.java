package yi.editor.framework.accelerator;

import yi.editor.EditorFrame;

/**
 * Unique identifier keys for all accelerators in {@link EditorFrame}. Each identifier
 * maps to an {@link EditorAcceleratorManager.Accelerator}, which can be retrieved
 * using {@link EditorAcceleratorManager#getAccelerator(EditorAcceleratorId)}.
 *
 * @see EditorAcceleratorManager Accelerator management in the editor
 */
public enum EditorAcceleratorId {

    UNDO("undo"),
    REDO("redo"),

    TOGGLE_PERSPECTIVE_COMPACT("togglePerspectiveCompact"),
    TOGGLE_PERSPECTIVE_REVIEW("togglePerspectiveReview"),

    TOGGLE_BOARD_COORDINATES("toggleBoardCoordinates"),

    NEW_GAME("newGame"),
    OPEN_GAME("openGame"),
    SAVE_GAME("saveGame"),
    SAVE_AS_GAME("saveAsGame"),

    ;

    private final String id;

    EditorAcceleratorId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}