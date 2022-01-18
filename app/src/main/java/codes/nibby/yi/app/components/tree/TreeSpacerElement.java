package codes.nibby.yi.app.components.tree;

/**
 * An element used solely to occupy a grid space so that subsequent nodes do not consider this grid
 * when laying out the tree structure. This is most frequently used to reserve grids that are used to
 * render track lines. Though in theory this element can be used wherever we want to prohibit {@link GameTreeStructure}
 * from placing an element there.
 */
final class TreeSpacerElement implements TreeElement {

    private final int gridX;
    private final int gridY;

    TreeSpacerElement(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
    }

    @Override
    public int getGridX() {
        return gridX;
    }

    @Override
    public int getGridY() {
        return gridY;
    }

    @Override
    public void setHighlighted(boolean highlighted) {
        // Do nothing
    }

    @Override
    public boolean isHighlighted() {
        return false;
    }
}
