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
    private int logicalY;

    private TreeElement parent;
    private final GameNode<GoGameStateUpdate> node;

    public TreeNodeElement(@Nullable TreeElement parent, GameNode<GoGameStateUpdate> node, int x, int y) {
        this.parent = parent;
        this.node = node;
    }

    @Override
    public Optional<TreeElement> getParent() {
        return Optional.empty();
    }

    @Override
    public int getLogicalX() {
        return logicalX;
    }

    @Override
    public int getLogicalY() {
        return logicalY;
    }
}
