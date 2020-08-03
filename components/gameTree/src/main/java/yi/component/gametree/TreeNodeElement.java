package yi.component.gametree;

import org.jetbrains.annotations.Nullable;
import yi.core.go.GameNode;

import java.util.Optional;

/**
 * A wrapper for each node in the game tree.
 */
final class TreeNodeElement implements TreeElement {

    private final int gridX;
    private final int gridY;

    private final TreeNodeElement parent;
    private final GameNode node;
    private boolean highlighted = false;

    public TreeNodeElement(@Nullable TreeNodeElement parent, GameNode node, int gridX, int gridY) {
        this.parent = parent;
        this.node = node;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    /**
     *
     * @return The parent node element for the node wrapped by this element.
     */
    public Optional<TreeNodeElement> getParent() {
        return Optional.ofNullable(parent);
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
        this.highlighted = highlighted;
    }

    @Override
    public boolean isHighlighted() {
        return highlighted;
    }

    public GameNode getNode() {
        return node;
    }
}
