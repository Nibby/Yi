package yi.component.gametree;

import org.jetbrains.annotations.Nullable;
import yi.core.common.GameNode;
import yi.core.go.GoGameStateUpdate;

import java.util.Optional;

/**
 * A wrapper for each node in the game tree.
 */
final class TreeNodeElement implements TreeElement {

    private final int gridX;
    private final int gridY;

    private final TreeNodeElement parent;
    private final GameNode<GoGameStateUpdate> node;
    private boolean highlighted = false;

    public TreeNodeElement(@Nullable TreeNodeElement parent, GameNode<GoGameStateUpdate> node, int x, int y) {
        this.parent = parent;
        this.node = node;
        this.gridX = x;
        this.gridY = y;
    }

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

    public GameNode<GoGameStateUpdate> getNode() {
        return node;
    }
}
