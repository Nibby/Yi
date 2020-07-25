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
//TODO: The algorithm to lay out the tree structure can be improved. Specifically, there is actually
//      no need to use TreeSpacerElement to pad spaces for subsequent branches, because it
//      can be calculated dynamically by looking at the last node in the branch and finding an
//      appropriate column. This way we save computation (without having to call
//      TreeElementPositionStorage#adjustAllNodesToSameColumn(TreeNodeElement, TreeNodeElement, int)
//      on each update. As an MVP, I'll let it slide, but will be logging an issue for this later.
final class GameTreeStructure {

    private final GoGameModel gameModel;
    private final TreeElementManager treeElementManager;

    public GameTreeStructure(GoGameModel gameModel) {
        this.gameModel = gameModel;
        this.treeElementManager = new TreeElementManager();

        reconstruct();
    }

    /**
     * Retrieves all the elements within the defined grid-space rectangle.
     *
     * @param topLeftX Top left grid x
     * @param topLeftY Top left grid y
     * @param bottomRightX Bottom right grid x
     * @param bottomRightY Bottom right grid y
     * @return All elements within the rectangle defined by the two points
     */
    public Collection<TreeElement> getElementsWithinBounds(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
        Collection<TreeElement> elementsWithinBounds = new HashSet<>();

        for (int x = topLeftX; x < bottomRightX; ++x) {
            for (int y = topLeftY; y < bottomRightY; ++y) {
                getElement(x, y).ifPresent(elementsWithinBounds::add);
            }
        }

        return elementsWithinBounds;
    }

    /**
     * Retrieves the element at the given grid space.
     */
    public Optional<TreeElement> getElement(int gridX, int gridY) {
        return treeElementManager.positionStorage.getElement(gridX, gridY);
    }

    public Collection<TreeElement> getElements() {
        return treeElementManager.getAllElements();
    }

    public void reconstruct() {
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
    private void createSubtree(TreeNodeElement parentElement, GameNode<GoGameStateUpdate> treeParent) {
        var nodesToCreateSubtree = new Stack<TreeNodeElement>();
        var currentNode = treeParent;

        TreeNodeElement lastParent = parentElement;

        while (currentNode != null) {
            var currentNodeElement = treeElementManager.addNode(lastParent, currentNode);

            if (currentNode.hasOtherVariations()) {
                // Revisit this later to create its subtree
                nodesToCreateSubtree.push(currentNodeElement);
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
            var children = branchingPoint.getNode().getChildren();

            for (var child : children) {
                if (children.indexOf(child) == 0)
                    continue; // Main variation, already built from the while loop above, so we skip it here.

                // As a MVP I think this recursion is fine. However...
                // If this starts to throw StackOverflowException, it's probably because the game file has
                // too many variations. We might have to consider a different approach at that point...
                createSubtree(branchingPoint, child);
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

        public TreeNodeElement addNode(TreeNodeElement parentElement, GameNode<GoGameStateUpdate> node) {
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

        public TreeNodeElement addNode(TreeNodeElement nodeParent, GameNode<GoGameStateUpdate> node) {
            int[] vacantPosition = makeRoomForNextVacantPosition(nodeParent);
            int x = vacantPosition[0];
            int y = vacantPosition[1];

            if (isPositionOccupied(x, y)) {
                throw new IllegalArgumentException("The position at (" + x + ", " + y + ") is already occupied");
            }

            var nodeElement = new TreeNodeElement(nodeParent, node, x, y);
            addElement(nodeElement);
            adjustAllNodesToSameColumn(nodeParent, nodeElement, x);

            return nodeElement;
        }

        /*
         * Ensures that all the nodes in a given branch are aligned on the same column. This can be improved.
         * See the to-do note at the beginning of the parent class.
         */
        private void adjustAllNodesToSameColumn(TreeNodeElement nodeParent, TreeNodeElement node, int logicalX) {
            TreeNodeElement currentParent = nodeParent;
            TreeNodeElement currentNodeElement = node;

            while (currentParent != null && currentNodeElement != null) {
                if (currentParent.getNode().getChildren().indexOf(currentNodeElement.getNode()) == 0) {
                    int parentLogicalX = currentParent.getLogicalX();
                    if (logicalX > parentLogicalX) {
                        // Our child is not in the same column as the parent, likely because another
                        // branch further down has occupied the space...
                        //
                        // i.e.
                        // [node]====|
                        //   |     [this]=====|
                        // [node]           [this*] <-- We're here
                        //   |-------|
                        // [node]  [node]
                        //
                        // In this case, the child is part of the main variation of this branch,
                        // for aesthetic purposes, we wish to shift the parent column so that it is
                        // the same as the child.
                        currentParent.setLogicalX(logicalX);

                        // Since the column has shifted, also ensure the track space is reserved.
                        for (int column = parentLogicalX; column <= logicalX; column++) {
                            addElement(new TreeSpacerElement(column, currentParent.getLogicalY() - 1));
                        }
                    }

                    Optional<TreeNodeElement> parent = currentParent.getParent();
                    if (parent.isPresent()) {
                        currentNodeElement = currentParent;
                        currentParent = parent.get();
                    } else {
                        currentParent = null;
                    }
                } else {
                    currentParent = null;
                }
            }

        }

        /**
         * Gets the next nearest vacant position to place the child of this parent element. The parent
         * must be part of the game tree. If the parent element is null, the vacant position will be the
         * position of the tree root node.
         *
         * This method will change the state of the position storage by adding tracks to grids that are
         * unsuitable to place new nodes.
         *
         * @param parentElement The element to find vacant child node position for
         * @return An array of size 2, representing the (x, y) position in the element position space.
         *
         * @throws IllegalArgumentException If the element is not part of the position storage.
         */
        private int[] makeRoomForNextVacantPosition(@Nullable TreeElement parentElement) {
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

                // This grid is blocked because another branch further down has a node/track here.
                // In order to find a suitable place to put our current node, we will also need to
                // reserve the grid one space above this point to draw our track lines.
                //
                // i.e. we're reserving the space marked by the '===' using the following track element
                // [node]============|
                //   |-------|       |
                // [node]  [node]  [node]
                var trackReservedGrid = new TreeSpacerElement(childX, childY - 1);
                addElement(trackReservedGrid);
            }

            return new int[] { childX, childY };
        }

        private boolean isPositionOccupied(int x, int y) {
            if(!elementPositions.containsKey(x))
                return false;

            return elementPositions.get(x).containsKey(y);
        }

        private void addElement(TreeElement element) {
            int x = element.getLogicalX();
            int y = element.getLogicalY();

            elementPositions.putIfAbsent(x, new HashMap<>());
            var yMap = elementPositions.get(x);
            yMap.put(y, element);
        }

        public void clear() {
            elementPositions.keySet().forEach(xKey -> elementPositions.get(xKey).clear());
        }

        public Optional<TreeElement> getElement(int x, int y) {
            if (elementPositions.containsKey(x)) {
                return Optional.ofNullable(elementPositions.get(x).get(y));
            }

            return Optional.empty();
        }
    }
}
