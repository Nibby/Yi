package yi.component.treeviewer;

/**
 * A displayable object on the game tree. Each element is constructed and managed by the
 * {@link GameTreeStructure}.
 */
public interface TreeElement {

    /**
     * This is not to be confused with drawing coordinates.
     *
     * @return The x ordinate of the element in the tree structure.
     */
    int getGridX();

    /**
     * This is not to be confused with drawing coordinates.
     *
     * @return The y ordinate of the element in the tree structure.
     */
    int getGridY();

    /**
     * Whether to apply special rendering to the element (i.e. when mouse hovers over the object).
     *
     * @param highlighted true if it is to be highlighted.
     */
    void setHighlighted(boolean highlighted);

    /**
     *
     * @return true if the element is currently highlighted.
     */
    boolean isHighlighted();

}
