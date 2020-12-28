package yi.component.treeviewer;

public enum NodeViewMode {

    COMPACT,
    LABELLED;

    public static NodeViewMode getDefaultValue() {
        return COMPACT;
    }
}
