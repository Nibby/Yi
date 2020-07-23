package yi.component.gametree;

import org.jetbrains.annotations.Nullable;
import yi.core.common.GameNode;
import yi.core.go.GoGameModel;
import yi.core.go.GoGameStateUpdate;

import java.util.*;

/**
 * This is the view model for the tree viewer. It determines the sizing and position for each node
 * in the game tree.
 */
final class GameTreeStructure {

    private GoGameModel gameModel;
    private final TreeElementManager treeElementManager;

    public GameTreeStructure(GoGameModel gameModel) {
        this.gameModel = gameModel;
        this.treeElementManager = new TreeElementManager();

        update();
    }

    public Collection<TreeElement> getElementsWithinBounds(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
        throw new IllegalStateException("Not Implemented!"); // TODO: Implement me
    }

    public Collection<TreeElement> getElements() {
        return treeElementManager.getAllElements();
    }

    public void update() {
        treeElementManager.reset();

        var currentMove = gameModel.getCurrentMove();
        var rootMove = currentMove.getRoot();

        createSubtree(null, rootMove);
    }

    /*
     * Algorithm:
     *
     * 1. Build the main variation branch
     * 2. Backtrack from the leaf node until a node with child size > 1 is encountered,
     *    and recursively call createSubtree with it as the parent.
     *
     * Branches are created from the child variation first so that variations closer towards the
     * root of the tree grows outwards.
     */
    private void createSubtree(TreeElement parentElement, GameNode<GoGameStateUpdate> treeParent) {
        var nodesToCreateSubtree = new Stack<ExpandableNode>();
        var currentNode = treeParent;

        TreeElement lastParent = parentElement;

        while (currentNode != null) {
            var currentNodeElement = treeElementManager.addNode(lastParent, currentNode);

            if (currentNode.hasOtherVariations()) {
                // Revisit this later to create its subtree
                var nodeToBranch = new ExpandableNode(currentNode, lastParent);
                nodesToCreateSubtree.push(nodeToBranch);
            }

            lastParent = currentNodeElement;

            if (currentNode.getChildren().size() == 0) {
                currentNode = null;
            } else {
                currentNode = currentNode.getChildren().get(0);
            }
        }

        while (nodesToCreateSubtree.size() > 0) {
            var branchingPoint = nodesToCreateSubtree.pop();

            var parentOfNodeToExpand = branchingPoint.getParentNode();
            var nonMainVariationChildren = branchingPoint.node.getChildren();

            for (var child : nonMainVariationChildren) {
                // As a MVP I think this recursion is fine. However...
                // If this starts to throw StackOverflowException, it's probably because the game file has
                // too many variations. We might have to consider a different approach at that point...
                createSubtree(parentOfNodeToExpand, child);
            }
        }
    }

    private static final class TreeElementManager {

        private Collection<TreeElement> allElements = new HashSet<>();
        private TreeElementPositionStorage positionStorage = new TreeElementPositionStorage();

        public void reset() {
            positionStorage.clear();
            allElements.clear();

            allElements = new HashSet<>();
            positionStorage = new TreeElementPositionStorage();
        }

        public TreeElement addNode(TreeElement parentElement, GameNode<GoGameStateUpdate> node) {
            var nodeElement = positionStorage.addNode(parentElement, node);
            allElements.add(nodeElement);

            return nodeElement;
        }

        public Collection<TreeElement> getAllElements() {
            return allElements;
        }
    }

    /**
     * Maps all the elements displayed on the game tree within an internal grid space, where (0,0) is the
     * top-left grid reserved for the root node. Subsequent nodes are mapped downwards (along the Y-axis),
     * and subsequent variations are mapped sideways (along the X-axis).
     */
    private static final class TreeElementPositionStorage {

        private final Map<Integer, Map<Integer, TreeElement>> elementPositions = new HashMap<>();

        public TreeNodeElement addNode(TreeElement nodeParent, GameNode<GoGameStateUpdate> node) {
            int[] position = findNextVacantPosition(nodeParent);
            int x = position[0];
            int y = position[1];

            if (isPositionOccupied(x, y)) {
                throw new IllegalArgumentException("The position at (" + x + ", " + y + ") is already occupied");
            }

            var nodeElement = new TreeNodeElement(nodeParent, node, x, y);

            elementPositions.putIfAbsent(x, new HashMap<>());
            var yMap = elementPositions.get(x);
            yMap.put(y, nodeElement);

            return nodeElement;
        }

        /**
         * Finds the next nearest vacant position to place the child of this parent element. The parent
         * must be part of the game tree. If the parent element is null, the vacant position will be the
         * position of the tree root node.
         *
         * @param parentElement The element to find vacant child node position for
         * @return An array of size 2, representing the (x, y) position in the element position space.
         *
         * @throws IllegalArgumentException If the element is not part of the position storage.
         */
        private int[] findNextVacantPosition(@Nullable TreeElement parentElement) {
            if (parentElement == null) {
                return new int[] { 0, 0 };
            }

            return _computeVacantPosition(parentElement);
        }

        private int[] _computeVacantPosition(TreeElement parentElement) {
            int parentX = parentElement.getLogicalX();
            int parentY = parentElement.getLogicalY();

            int childX = parentX;
            int childY = parentY + 1;

            while (isPositionOccupied(childX, childY)) {
                childX += 1;
            }

            return new int[] { childX, childY };
        }

        private boolean isPositionOccupied(int x, int y) {
            if(!elementPositions.containsKey(x))
                return false;

            return elementPositions.get(x).containsKey(y);
        }

        public void clear() {
            elementPositions.keySet().forEach(xKey -> elementPositions.get(xKey).clear());
        }
    }

    private static final class ExpandableNode {
        private final GameNode<GoGameStateUpdate> node;
        private final TreeElement parentNode;

        public ExpandableNode(GameNode<GoGameStateUpdate> node, TreeElement parentNode) {
            this.node = node;
            this.parentNode = parentNode;
        }

        public GameNode<GoGameStateUpdate> getNode() {
            return node;
        }

        public TreeElement getParentNode() {
            return parentNode;
        }
    }
}
