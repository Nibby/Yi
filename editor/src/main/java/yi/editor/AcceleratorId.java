package yi.editor;

public enum AcceleratorId {

    UNDO("undo"),
    REDO("redo"),

    TOGGLE_PERSPECTIVE_COMPACT("togglePerspectiveCompact"),
    TOGGLE_PERSPECTIVE_REVIEW("togglePerspectiveReview")
    ;

    private final String id;

    AcceleratorId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
