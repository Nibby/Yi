package yi.component.gametree;

import org.jetbrains.annotations.Nullable;
import yi.core.common.GameNode;
import yi.core.go.GoGameStateUpdate;

import java.util.Optional;

/**
 * A wrapper for each node in the game tree.
 */
final class TreeNodeElement implements TreeElement {

    private int logicalX;
    private final int logicalY;

    private final TreeNodeElement parent;
    private final GameNode<GoGameStateUpdate> node;

    public TreeNodeElement(@Nullable TreeNodeElement parent, GameNode<GoGameStateUpdate> node, int x, int y) {
        this.parent = parent;
        this.node = node;
        this.logicalX = x;
        this.logicalY = y;
    }

    public Optional<TreeNodeElement> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public int getLogicalX() {
        return logicalX;
    }

    @Override
    public int getLogicalY() {
        return logicalY;
    }

    public GameNode<GoGameStateUpdate> getNode() {
        return node;
    }

    public void setLogicalX(int x) {
        this.logicalX = x;
    }
}