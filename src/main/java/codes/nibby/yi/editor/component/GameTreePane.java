package codes.nibby.yi.editor.component;

import codes.nibby.yi.board.StoneRenderer;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.GameNode;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * A component for displaying the game tree structure visually.
 * Allows user input to navigate.
 *
 * TODO: Implement this later.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class GameTreePane extends GridPane implements GameListener {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private static final int COMPONENT_WIDTH = 24;
    private static final int COMPONENT_HEIGHT = 24;

    private int orientation = VERTICAL;
    private Map<GameNode, NodeElement> nodeMap;
    private ElementLayout layout;
    private GameEditorWindow editor;
    private Game game;
    private NodeElement currentNode;

    public GameTreePane(GameEditorWindow editor) {
        this.editor = editor;
    }

    private void rebuildTree() {
        nodeMap = new HashMap<>();
        layout = new ElementLayout();
        getChildren().clear();
        GameNode current = game.getGameTree();
        int row = 0, col = 0;
        buildBranch(current, row, col);
    }

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
            TrackElement track = new TrackElement(orientation, 0, col,firstNode.gridY - 1);
            add(track, track.gridX, track.gridY);
            col++;
        }

        if (firstNode.node.getParent() != null
                && firstNode.node.getParent().getChildren().indexOf(firstNode.node) != 0
                && firstNode.gridY >= 1) {
            int index = firstNode.node.getParent().getChildren().indexOf(firstNode.node);
            boolean lastChild = firstNode.node.getParent().getChildren().size() - 1 == index;
            int trackType = lastChild ? 2 : 1;
            TrackElement track = new TrackElement(orientation, trackType, col,firstNode.gridY - 1);
            add(track, track.gridX, track.gridY);
            System.out.println(lastChild);
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
    }

    private abstract static class AbstractElement extends Canvas {

        GraphicsContext g;
        int gridY, gridX;

        private AbstractElement(int gridX, int gridY) {
            g = getGraphicsContext2D();
            this.gridY = gridY;
            this.gridX = gridX;
        }
    }

    private static class NodeElement extends AbstractElement {

        private Game game;
        private GameNode node;

        private NodeElement(Game game, GameNode node, int gridX, int gridY) {
            super(gridX, gridY);
            this.game = game;
            this.node = node;
            setWidth(COMPONENT_WIDTH);
            setHeight(COMPONENT_HEIGHT);
            render();
        }

        public void render() {
            g.clearRect(0, 0, getWidth(), getHeight());
            if (game.getCurrentNode().equals(node)) {
                // TODO change aesthetics later
                g.setFill(Color.YELLOW);
                g.fillRect(0, 0, getWidth(), getHeight());
            } else {
                // TODO temporary
                g.setStroke(Color.BLACK);
                g.strokeRect(0, 0, getWidth(), getHeight());
            }

            int iconSize = (int) Math.min(getWidth(), getHeight());
            StoneRenderer.renderTexture(g, node.getColor(), iconSize, 0, 0);
        }
    }

    private static class TrackElement extends AbstractElement {
        private static final int TRACK_HORIZONTAL_LINE = 0;
        private static final int TRACK_HORIZONTAL_T = 1;
        private static final int TRACK_HORIZONTAL_CORNER = 2;
        private static final int TRACK_VERTICAL_LINE = 3;
        private static final int TRACK_VERTICAL_T = 4;
        private static final int TRACK_VERTICAL_CORNER = 5;

        private int trackType;
        private GameNode nodeParent;
        private GameNode nodeChild;
        private int row, col;

        private TrackElement(int orientation, int type, int col, int row) {
            super(col, row);
            this.row = row;
            this.col = col;
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

            g = getGraphicsContext2D();
            setWidth(COMPONENT_WIDTH);
            setHeight(COMPONENT_HEIGHT);
            g.setStroke(Color.BLACK);
            g.setLineWidth(2d);
            switch (trackType) {
                case TRACK_HORIZONTAL_LINE:
                    g.strokeLine(0, getHeight() / 2 - 1, getWidth(), getHeight() / 2 - 1);
                    break;
                case TRACK_HORIZONTAL_T:
                    g.strokeLine(0, getHeight() / 2 - 1, getWidth(), getHeight() / 2 - 1);
                    g.strokeLine(getWidth() / 2, getHeight() / 2 - 1, getWidth()/ 2, getHeight());
                    break;
                case TRACK_HORIZONTAL_CORNER:
                    g.strokeLine(0, getHeight() / 2 - 1, getWidth() / 2, getHeight() / 2 - 1);
                    g.strokeLine(getWidth() / 2, getHeight() / 2 - 1, getWidth()/ 2, getHeight());
                    break;

                case TRACK_VERTICAL_LINE:
                    g.strokeLine(getWidth() / 2, 0, getWidth()/ 2, getHeight());
                    break;
            }
            g.setLineWidth(1d);
        }
    }

    private static class ElementLayout {

        private Map<Integer, List<Integer[]>> columnData = new HashMap<>();
        private Map<Integer, List<NodeElement>> nodeData = new HashMap<>();

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
