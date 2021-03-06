package yi.component.treeviewer;

import javafx.geometry.Dimension2D;

/**
 * Manages the sizing properties of the elements in the game tree.
 */
class GameTreeElementSize {

    private NodeViewMode viewMode = NodeViewMode.getDefaultValue();

    public NodeViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(NodeViewMode viewMode) {
        this.viewMode = viewMode;
    }

    /**
     *
     * @return The dimension of each grid in the tree based on the {@link NodeViewMode}.
     */
    public Dimension2D getGridSize() {
        switch (viewMode) {
            case COMPACT: return new Dimension2D(20, 20);
            case LABELLED: return new Dimension2D(60, 20);
        }
        throw new IllegalStateException("Unimplemented view mode: " + viewMode.name());
    }
}
