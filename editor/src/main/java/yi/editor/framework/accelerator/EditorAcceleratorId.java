package yi.editor.framework.accelerator;

import yi.editor.EditorWindow;

/**
 * Unique identifier keys for all accelerators in {@link EditorWindow}. Each identifier
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

    TEST_ACCEL_1("testAccel1"),
    TEST_ACCEL_2("testAccel2"),
    ;

    private final String id;

    EditorAcceleratorId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
