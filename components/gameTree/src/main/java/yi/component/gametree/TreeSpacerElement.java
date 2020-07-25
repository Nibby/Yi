package yi.component.gametree;

/**
 * An element used solely to occupy a grid space so that subsequent nodes do not consider this grid
 * when laying out the tree structure.
 */
final class TreeSpacerElement implements TreeElement {

    private final int logicalX;
    private final int logicalY;

    TreeSpacerElement(int logicalX, int logicalY) {
        this.logicalX = logicalX;
        this.logicalY = logicalY;
    }

    @Override
    public int getLogicalX() {
        return logicalX;
    }

    @Override
    public int getLogicalY() {
        return logicalY;
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
