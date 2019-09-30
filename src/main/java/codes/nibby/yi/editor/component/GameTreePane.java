package codes.nibby.yi.editor.component;

import codes.nibby.yi.board.StoneRenderer;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.GameNode;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * A component for displaying the game tree structure visually.
 * Accepts mouse and keyboard input to navigate the game states.
 *
 * The tree pane is implemented using a GridPane, where each 'node'
 * is added as a separate 'NodeElement' element. TrackElements are
 * added where child branch exists to graphically indicate a branching
 * relationship.
 *
 * TODO: Implement this later.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class GameTreePane extends GridPane implements GameListener {

    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;

    private static final int COMPONENT_WIDTH = 24;
    private static final int COMPONENT_HEIGHT = 24;

    private Map<GameNode, NodeElement> nodeMap;
    private ElementLayout layout;
    private GameEditorWindow editor;
    private Game game;
    private NodeElement currentNode;
    private ScrollPane scrollPane;

    public GameTreePane(GameEditorWindow editor) {
        this.editor = editor;
    }

    /**
     * Clears all previous elements on the tree and reconstruct
     * from scratch.
     */
    private void rebuildTree() {
        nodeMap = new HashMap<>();
        layout = new ElementLayout();
        getChildren().clear();
        GameNode current = game.getGameTree();
        int row = 0, col = 0;
        buildBranch(current, row, col);
    }

    /**
     * Constructs a single branch from the tree, traversing through until the
     * end of the main variation. Child branches are constructed recursively this way.
     *
     * @param branchRoot The first node in the branch.
     * @param startCol GridPane X co-ordinate of the first node.
     * @param startRow GridPane Y co-ordinate of the first node.
     */
    private void buildBranch(GameNode branchRoot, int startCol, int startRow) {
        int gridX = startCol;
        int gridY = startRow;

        GameNode current = branchRoot;
        List<NodeElement> branchNodes = new ArrayList<>();
        Stack<GameNode> childNodes = new Stack<>();
        Map<GameNode, NodeElement> parentNodes = new HashMap<>();
        int size = 0;
        int start = branchRoot.getMoveNumber() - 1;
        do {
            // Build current variation
            int moveNumber = current.getMoveNumber();
            NodeElement _node = new NodeElement(game, current, gridX, gridY++);
            nodeMap.put(current, _node);
            layout.nodeData.putIfAbsent(moveNumber, new ArrayList<>());
            layout.nodeData.get(moveNumber).add(_node);
            branchNodes.add(_node);
            if (current.equals(game.getCurrentNode()))
                currentNode = _node;

            // Build child variation branches
            List<GameNode> children = current.getChildren();
            for (int i = 1; i < children.size(); i++) {
                GameNode child = children.get(i);
                childNodes.push(child);
                parentNodes.put(child, _node);
            }

            size++;
            if (current.hasChildren()) {
                current = current.getChildren().get(0);

            } else
                current = null;
        } while (current != null);

        int end = start + size;
        int col = startCol;
        NodeElement firstNode = branchNodes.get(0);
        while (!layout.isSegmentAvailable(col, start, end)) {
            TrackElement track = new TrackElement(VERTICAL, 0, col,firstNode.gridY - 1);
            add(track, track.gridX, track.gridY);
            col++;
        }

        if (firstNode.node.getParent() != null
                && firstNode.node.getParent().getChildren().indexOf(firstNode.node) != 0
                && firstNode.gridY >= 1) {
            int index = firstNode.node.getParent().getChildren().indexOf(firstNode.node);
            boolean lastChild = firstNode.node.getParent().getChildren().size() - 1 == index;
            int trackType = lastChild ? 2 : 1;
            TrackElement track = new TrackElement(VERTICAL, trackType, col,firstNode.gridY - 1);
            add(track, track.gridX, track.gridY);
        }

        layout.columnData.putIfAbsent(col, new ArrayList<>());
        layout.columnData.get(col).add(new Integer[] { start, end });
        for (NodeElement _node : branchNodes) {
            add(_node, col, _node.gridY);
        }

        // Handle child nodes
        while (!childNodes.isEmpty()) {
            GameNode node = childNodes.pop();
            int index = node.getParent().getChildren().indexOf(node);
            buildBranch(node, col + index, node.getMoveNumber());
        }
    }

    public void setScrollPane(ScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    @Override
    public void gameInitialized(Game game) {
        this.game = game;
        rebuildTree();
    }

    @Override
    public void gameCurrentMoveUpdate(GameNode currentMove, boolean newMove) {
        if (!newMove) {
            assert currentNode != null;
            currentNode.render();
            currentNode = nodeMap.get(currentMove);
            currentNode.render();
        } else {
            // TODO temporary. Ideally new moves are appended to the existing structure.
            rebuildTree();
        }

        // TODO Keep the current node in the viewport
    }

    private abstract static class AbstractElement extends Canvas {

        GraphicsContext g;
        int gridY, gridX;

        private AbstractElement(int gridX, int gridY) {
            g = getGraphicsContext2D();
            this.gridY = gridY;
            this.gridX = gridX;
        }

        public abstract void render();
    }

    /**
     * Represents a GameNode rendered on the game tree.
     */
    private static class NodeElement extends AbstractElement {

        private Game game;
        private GameNode node;
        private boolean hovered = false;

        private NodeElement(Game game, GameNode node, int gridX, int gridY) {
            super(gridX, gridY);
            this.game = game;
            this.node = node;
            setWidth(COMPONENT_WIDTH);
            setHeight(COMPONENT_HEIGHT);
            addEventHandler(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
            addEventHandler(MouseEvent.MOUSE_EXITED, this::mouseExited);
            addEventHandler(MouseEvent.MOUSE_MOVED, this::mouseMoved);
            addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
            render();
        }

        private void mousePressed(MouseEvent t) {
            game.setCurrentNode(node, false);
        }

        private void mouseMoved(MouseEvent t) {

        }

        private void mouseExited(MouseEvent t) {
            hovered = false;
            setCursor(Cursor.OPEN_HAND);
            render();
        }

        private void mouseEntered(MouseEvent t) {
            hovered = true;
            setCursor(Cursor.HAND);
            render();
        }

        @Override
        public void render() {
            g.clearRect(0, 0, getWidth(), getHeight());

            if (game.getCurrentNode().equals(node)) {
                // TODO change aesthetics later
                g.setFill(Color.YELLOW);
                g.fillRect(0, 0, getWidth(), getHeight());
            } else if (hovered) {
                g.setFill(Color.LAVENDER);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            // Draw tracks
            g.setLineWidth(2d);
            g.setStroke(Color.DARKGRAY);
            if (node.getParent() != null) {
                g.strokeLine(getWidth() / 2, 0, getWidth() / 2, getHeight() / 2);
            }

            if (node.hasChildren()) {
                g.strokeLine(getWidth() / 2, getHeight() / 2, getWidth() / 2, getHeight());

                if (node.getChildren().size() > 1)
                    g.strokeLine(getWidth() / 2, getHeight() / 2, getWidth(), getHeight() / 2);
            }

            // Draw node icon
            int size = (int) Math.min(getWidth() / 3 * 2, getHeight() / 3 * 2);
            StoneRenderer.renderTexture(g, node.getColor(), size, getWidth() / 2 - (size / 2f), getHeight() / 2 - (size / 2f));
        }
    }

    /**
     * Represents a child variation branch icon.
     */
    private static class TrackElement extends AbstractElement {
        private static final int TRACK_HORIZONTAL_LINE = 0;
        private static final int TRACK_HORIZONTAL_T = 1;
        private static final int TRACK_HORIZONTAL_CORNER = 2;
        private static final int TRACK_VERTICAL_LINE = 3;
        private static final int TRACK_VERTICAL_T = 4;
        private static final int TRACK_VERTICAL_CORNER = 5;

        private int trackType;
        boolean debug = true;

        private TrackElement(int orientation, int type, int col, int row) {
            super(col, row);
            switch (type) {
                case 0:
                    trackType = (orientation == VERTICAL) ? TRACK_HORIZONTAL_LINE : TRACK_VERTICAL_LINE;
                    break;
                case 1:
                    trackType = (orientation == VERTICAL) ? TRACK_HORIZONTAL_T : TRACK_VERTICAL_T;
                    break;
                case 2:
                    trackType = (orientation == VERTICAL) ? TRACK_HORIZONTAL_CORNER : TRACK_VERTICAL_CORNER;
                    break;
            }

            setWidth(COMPONENT_WIDTH);
            setHeight(COMPONENT_HEIGHT);

            addEventHandler(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
            addEventHandler(MouseEvent.MOUSE_EXITED, this::mouseExited);
            addEventHandler(MouseEvent.MOUSE_MOVED, this::mouseMoved);
            addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
            render();
        }

        private void mousePressed(MouseEvent t) {
        }

        private void mouseMoved(MouseEvent t) {

        }

        private void mouseExited(MouseEvent t) {

        }

        private void mouseEntered(MouseEvent t) {
        }

        @Override
        public void render() {
            g.clearRect(0, 0, getWidth(), getHeight());
            // TODO track color subject to change
            g.setStroke(Color.DARKGRAY);
            g.setLineWidth(2d);

            switch (trackType) {
                case TRACK_HORIZONTAL_LINE:
                    g.strokeLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                    break;
                case TRACK_HORIZONTAL_T:
                    g.strokeLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                    g.strokeLine(getWidth() / 2, getHeight() / 2, getWidth()/ 2, getHeight());
                    break;
                case TRACK_HORIZONTAL_CORNER:
                    g.strokeLine(0, getHeight() / 2, getWidth() / 2, getHeight() / 2);
                    g.strokeLine(getWidth() / 2, getHeight() / 2, getWidth()/ 2, getHeight());
                    break;

                case TRACK_VERTICAL_LINE:
                    g.strokeLine(getWidth() / 2, 0, getWidth()/ 2, getHeight());
                    break;
                    // TODO rest of the vertical lines
            }
            g.setLineWidth(1d);
        }
    }

    /**
     * A helper class storing tree node positions. It is used to calculate the
     * position of new branches.
     */
    private static class ElementLayout {

        private Map<Integer, List<Integer[]>> columnData = new HashMap<>();
        private Map<Integer, List<NodeElement>> nodeData = new HashMap<>();

        /**
         * Checks if a given section does not have nodes.
         *
         * @param column The column index
         * @param start Segment start index
         * @param end Segment end index
         * @return Whether the segment is available for new branches.
         */
        private boolean isSegmentAvailable(int column, int start, int end) {
            List<Integer[]> segmentData = columnData.get(column);
            if (segmentData == null) {
                columnData.put(column, new ArrayList<>());
                return true;
            }

            // Avoid stepping into a column where an existing branch line extends further out
            List<Integer[]> nextColumn;
            int c = column;
            while ((nextColumn = columnData.get(c + 1)) != null) {
                for (Integer[] segment : nextColumn) {
                    if (start <= segment[0] && end >= segment[0] ||
                            start <= segment[1] && end >= segment[1] ||
                            start >= segment[0] && end <= segment[1])
                        return false;

                }
                c++;
            }

            for (Integer[] segment : segmentData) {
                if (start <= segment[0] && end >= segment[0] ||
                        start <= segment[1] && end >= segment[1] ||
                        start >= segment[0] && end <= segment[1])
                    return false;

            }
            return true;
        }
    }
}
