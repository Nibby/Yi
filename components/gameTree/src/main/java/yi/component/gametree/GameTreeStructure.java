package yi.component.gametree;

import org.jetbrains.annotations.Nullable;
import yi.core.go.GameNode;
import yi.core.go.GameModel;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles the layout presentation of the nodes in the game tree. Internally, the tree structure is
 * hosted within a grid space, where (0, 0) is on top left, and denotes the root element of the tree.
 * Elements are enumerated downwards (positive y-axis) and sideways (positive x-axis).
 * <p/>
 *
 */
final class GameTreeStructure {

    private final GameModel gameModel;
    private final TreeElementManager treeElementManager;

    public GameTreeStructure(GameModel gameModel) {
        this.gameModel = gameModel;
        this.treeElementManager = new TreeElementManager();

        reconstruct();
    }

    public void reconstruct() {
        treeElementManager.reset();
        createSubtree(null, gameModel.getRootNode());
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
    private void createSubtree(TreeNodeElement parentElement, GameNode treeParent) {
        var nodesToCreateSubtree = new Stack<TreeNodeElement>();
        var currentNode = treeParent;

        TreeNodeElement lastParent = parentElement;

        while (currentNode != null) {
            var currentNodeElement = treeElementManager.addNode(lastParent, currentNode, treeParent);

            if (currentNode.hasAlternativeNextMoves()) {
                // Revisit this later to create its subtree
                nodesToCreateSubtree.push(currentNodeElement);
            }

            lastParent = currentNodeElement;
            currentNode = currentNode.getNextMoveInMainBranch();
        }

        while (nodesToCreateSubtree.size() > 0) {
            var branchingPoint = nodesToCreateSubtree.pop();
            var variations = branchingPoint.getNode().getNextMovesExcludingMainBranch();

            for (var child : variations) {
                // As a MVP I think this recursion is fine. However...
                // If this starts to throw StackOverflowException, it's probably because the game file has
                // too many variations. We might have to consider a different approach at that point...
                createSubtree(branchingPoint, child);
            }
        }
    }

    /**
     * Retrieves the element at the given grid space.
     */
    public Optional<TreeNodeElement> getNodeElement(int gridX, int gridY) {
        return treeElementManager.positionStorage.getElement(gridX, gridY);
    }

    /**
     *
     * @return All the {@link TreeNodeElement} added so far in the tree structure.
     */
    public Collection<TreeNodeElement> getNodeElements() {
        return treeElementManager.getNodeElements();
    }

    /**
     *
     * @return All the elements added so far in the tree structure.
     */
    public Collection<TreeElement> getAllElements() {
        return treeElementManager.getAllElements();
    }

    public boolean setHighlightedGrid(int x, int y) {
        return treeElementManager.setHighlightedGrid(x, y);
    }

    private static final class TreeElementManager {

        private Collection<TreeElement> allElements = new HashSet<>();
        private final Collection<TreeNodeElement> nodeElements = new HashSet<>();
        private TreeElementPositionStorage positionStorage = new TreeElementPositionStorage();
        private TreeElement currentHighlight;

        private void reset() {
            positionStorage.clear();
            allElements.clear();

            allElements = new HashSet<>();
            positionStorage = new TreeElementPositionStorage();
        }

        /**
         * Wraps a new node as an element and determine its location in the tree structure. This is the entry point to the
         * tree structure spacing algorithm.
         *
         * @param parentElement The node element of the parent node of <code>nodeToAdd</code>
         * @param nodeToAdd The new node to be added to the position storage.
         * @param firstNodeInThisBranch The first node in the branch of <code>nodeToAdd</code>
         * @return A new {@link TreeNodeElement} that wraps the <code>nodeToAdd</code>
         */
        public TreeNodeElement addNode(TreeNodeElement parentElement, GameNode nodeToAdd, GameNode firstNodeInThisBranch) {
            var nodeElement = positionStorage.addNode(parentElement, nodeToAdd, firstNodeInThisBranch);
            allElements.add(nodeElement);
            nodeElements.add(nodeElement);

            return nodeElement;
        }

        public Collection<TreeElement> getAllElements() {
            return allElements;
        }

        public Collection<TreeNodeElement> getNodeElements() { return nodeElements; }

        /**
         * A highlighted grid may have some special rendering on the grid space or the element stored on it.
         * Only one grid may be highlighted at a time. Highlighting a new grid will automatically unhighlight the
         * previous.
         *
         * @param x X ordinate of the highlighted grid
         * @param y Y ordinate of the highlighted gri d
         * @return true if an element exists on that position.
         */
        public boolean setHighlightedGrid(int x, int y) {
            AtomicBoolean success = new AtomicBoolean(false);

            if (currentHighlight != null) {
                currentHighlight.setHighlighted(false);
            }

            positionStorage.getElement(x, y).ifPresent(newHighlight -> {
                newHighlight.setHighlighted(true);
                currentHighlight = newHighlight;
                success.set(true);
            });

            return success.get();
        }
    }

    /**
     * Maps all the elements displayed on the game tree within an internal grid space, where (0,0) is the
     * top-left grid reserved for the root node. Subsequent nodes are mapped downwards (along the Y-axis),
     * and subsequent variations are mapped sideways (along the X-axis).
     */
    private static final class TreeElementPositionStorage {

        private final Map<Integer, Map<Integer, TreeElement>> elementPositions = new HashMap<>();
        private final Map<GameNode, Integer> branchHeadToColumn = new HashMap<>();

        public TreeNodeElement addNode(TreeNodeElement nodeParent, GameNode nodeToAdd, GameNode firstNodeInThisBranch) {
            int[] vacantPosition = prepareForNextNode(nodeParent, nodeToAdd, firstNodeInThisBranch);
            int x = vacantPosition[0];
            int y = vacantPosition[1];

            assertNotOccupied(x, y);

            var nodeElement = new TreeNodeElement(nodeParent, nodeToAdd, x, y);
            addElement(nodeElement);

            return nodeElement;
        }

        private void removeNode(TreeElement nodeToRemove) {
            int x = nodeToRemove.getGridX();
            int y = nodeToRemove.getGridY();

            if (elementPositions.containsKey(x)) {
                var elementThere = elementPositions.get(x).get(y);
                if (elementThere != null && elementThere.equals(nodeToRemove)) {
                    elementPositions.get(x).remove(y, elementThere);
                } else {
                    throw new IllegalStateException("There is a different node at (" + x + ", " + y + ")");
                }
            }
        }

        private void assertNotOccupied(int x, int y) {
            if (isPositionOccupied(x, y)) {
                throw new IllegalArgumentException("The position at (" + x + ", " + y + ") is already occupied");
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
        private int[] prepareForNextNode(@Nullable TreeNodeElement parentElement, GameNode nodeToAdd, GameNode firstNodeInThisBranch) {
            if (nodeToAdd.equals(firstNodeInThisBranch)) {
                if (!branchHeadToColumn.containsKey(firstNodeInThisBranch)) {
                    computeColumnForNewBranch(parentElement, firstNodeInThisBranch);
                    int columnToUse = branchHeadToColumn.get(firstNodeInThisBranch);

                    if (parentElement != null) {
                        // All columns to the left of this one is unavailable, reserve track line space
                        for (int column = parentElement.getGridX() + 1; column <= columnToUse; ++column) {
                            var reservedGridForTrack = new TreeSpacerElement(column, parentElement.getGridY());
                            addElement(reservedGridForTrack);

                            // Block all internal space between the parent branch and this branch so that new branches
                            // are created on the outside. This will avoid all sorts of internal collisions.
                            if (column < columnToUse && getElement(column, parentElement.getGridY() + 1).isEmpty()) {
                                var blockedGridForSubsequentBranches = new TreeSpacerElement(column, parentElement.getGridY() + 1);
                                addElement(blockedGridForSubsequentBranches);
                            }
                        }
                    }
                }
            }

            if (parentElement == null) {
                return new int[] { 0, 0 }; // Root node
            }

            int nextNodeX = branchHeadToColumn.get(firstNodeInThisBranch);
            int nextNodeY = parentElement.getGridY() + 1;

            return new int[] { nextNodeX, nextNodeY };
        }

        private boolean isPositionOccupied(int x, int y) {
            if(!elementPositions.containsKey(x))
                return false;

            return elementPositions.get(x).containsKey(y);
        }

        private void addElement(TreeElement element) {
            int x = element.getGridX();
            int y = element.getGridY();

            elementPositions.putIfAbsent(x, new HashMap<>());
            var yMap = elementPositions.get(x);

            var itemHere = yMap.get(y);
            if (itemHere instanceof TreeNodeElement) {
                throw new IllegalStateException("Overwriting existing node element at (" + x + ", " + y + ")");
            }

            yMap.put(y, element);
        }

        /**
         * Removes all cached element positions within the storage.
         */
        void clear() {
            elementPositions.keySet().forEach(xKey -> elementPositions.get(xKey).clear());
        }

        /**
         * Retrieves a tree node element at the given co-ordinate.
         *
         * @param x X ordinate of the node element
         * @param y Y ordinate of the node element
         * @return The element if one is present at the location, or {@link Optional#empty()} if grid is empty, or
         *         it is not a {@link TreeNodeElement}
         */
        public Optional<TreeNodeElement> getElement(int x, int y) {
            if (elementPositions.containsKey(x)) {
                var itemHere = elementPositions.get(x).get(y);

                if (itemHere instanceof TreeNodeElement) {
                    return Optional.of((TreeNodeElement) itemHere);
                }
            }

            return Optional.empty();
        }

        /*
            Determines a suitable column to house all the nodes in the new branch such that all the nodes will be
            displayed in a single column.
         */
        private void computeColumnForNewBranch(@Nullable TreeNodeElement parentElementOfFirstNode, GameNode firstNodeInThisBranch) {
            int columnToUse = parentElementOfFirstNode != null ? parentElementOfFirstNode.getGridX() + 1 : 0; // Use first column for root
            int currentGridY = parentElementOfFirstNode != null ? parentElementOfFirstNode.getGridY() + 1 : 0; // Use first row for root

            var currentNode = firstNodeInThisBranch;

            while (currentNode != null) {
                while (isPositionOccupied(columnToUse, currentGridY)) {
                    ++columnToUse;
                }

                var nextMove = currentNode.getNextMoveInMainBranch();

                if (nextMove != null) {
                    currentNode = nextMove;
                    ++currentGridY;
                } else {
                    break;
                }
            }


            branchHeadToColumn.put(firstNodeInThisBranch, columnToUse);
        }
    }
}
