package codes.nibby.yi.editor.component;

import codes.nibby.yi.board.Stone;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.GameNode;
import codes.nibby.yi.utility.CanvasContainer;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
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
public class GameTreePane extends BorderPane implements GameListener {

    private static final int DRAW_X_MARGIN = 10;
    private static final int DRAW_Y_MARGIN = 10;

    private GameEditorWindow editor;
    private CanvasContainer container;
    private GameTreeCanvas canvas;
    private ScrollBar hScroll; // Horizontal
    private ScrollBar vScroll; // Vertical
    private FlowPane corner;
    private GameNode currentNode;
    private GameTreeNode currentTreeNode;

    // Scroll offsets
    private double xScroll = 0d;
    private double yScroll = 0d;

    private Map<Integer, List<Integer[]>> columnData = new HashMap<>();
    private Map<Integer, List<GameTreeNode>> nodeData = new HashMap<>();
    private int maxColumns = 0;
    private int maxMoves = 0;


    public GameTreePane(GameEditorWindow editor) {
        this.editor = editor;
        getStyleClass().add("editor_ui_gametree");

        editor.getGame().addGameListener(this);
        this.currentNode = editor.getGame().getCurrentNode();
        this.setPrefHeight(200);

        canvas = new GameTreeCanvas(this);
        canvas.setOnScroll(e -> {
            if (!vScroll.isVisible())
                return;
            double delta = e.getDeltaY();
            double value = vScroll.getValue();
            if (delta > 0) {
                value -= delta;
                if (value < vScroll.getMin())
                    value = vScroll.getMin();
            } else {
                value -= delta;
                if (value > vScroll.getMax())
                    value = vScroll.getMax();
            }
            vScroll.setValue(value);
        });
        container = new CanvasContainer(canvas);
        container.widthProperty().addListener(e -> {
            render();
        });
        container.heightProperty().addListener(e -> {
            render();
        });
        setCenter(container);

        hScroll = new ScrollBar();
        hScroll.getStyleClass().add("treeview_scroll_bar");
        hScroll.setMin(0);
        hScroll.setOrientation(Orientation.HORIZONTAL);
        hScroll.setManaged(false);
        hScroll.setVisible(false);
        hScroll.valueProperty().addListener(e -> {
            xScroll = hScroll.getValue();
            render();
        });
        BorderPane bottom = new BorderPane();
        bottom.setCenter(hScroll);
        corner = new FlowPane();
        corner.getStyleClass().add("corner");
        corner.setMaxWidth(16);
        bottom.setRight(corner);
        bottom.setMinWidth(0);
        setBottom(bottom);

        vScroll = new ScrollBar();
        vScroll.getStyleClass().add("treeview_scroll_bar");
        vScroll.setMin(0);
        vScroll.setOrientation(Orientation.VERTICAL);
        vScroll.setManaged(false);
        vScroll.setVisible(false);
        vScroll.valueProperty().addListener(e -> {
            yScroll = vScroll.getValue();
            render();
        });
        setRight(vScroll);

        reloadTree();

        vScroll.setVisible(false);
        vScroll.setManaged(false);
        hScroll.setVisible(false);
        hScroll.setManaged(false);
        corner.setVisible(false);
        corner.setManaged(false);
    }

    /**
     * Reconstructs the widget structure and recalculates all positioning.
     */
    private void reloadTree() {
        nodeData.clear();
        columnData.clear();
        createMoveBranch(editor.getGame().getGameTree(), null, 0);
        updateComponents();
        render();
    }

    private void updateNodeView() {
        // Keeping currently highlighted tree node within view
        double nodeX = currentTreeNode.getX();
        double nodeY = currentTreeNode.getY();
        double nodeWidth = GameTreeNode.DISPLAY_WIDTH;
        double nodeHeight = GameTreeNode.DISPLAY_HEIGHT;
        double offsetX = hScroll.getValue();
        double offsetY = vScroll.getValue();
        double width = getWidth();
        double height = getHeight();

        if (nodeY + nodeHeight > offsetY + height) {
            double value = nodeY - GameTreeNode.DISPLAY_HEIGHT;
            if (value > vScroll.getMax())
                value = vScroll.getMax();
            vScroll.setValue(value);
        }
        if (nodeY < offsetY) {
            double value = nodeY - height + GameTreeNode.DISPLAY_HEIGHT;
            if (value < vScroll.getMin())
                value = vScroll.getMin();
            vScroll.setValue(value);
        }
        if (nodeX + nodeWidth > offsetX + width) {
            double value = nodeX - GameTreeNode.DISPLAY_WIDTH;
            if (value > hScroll.getMax())
                value = hScroll.getMax();
            hScroll.setValue(value);
        }
        if (nodeX < offsetX) {
            double value = nodeX - DRAW_X_MARGIN;
            if (value < hScroll.getMin())
                value = hScroll.getMin();
            hScroll.setValue(value);
        }
    }

    private void updateComponents() {
        // TODO: Fix the max values of the scroll bars at small current values

        // Calculate sizing
        double tWidth = 2 * DRAW_X_MARGIN + maxColumns * GameTreeNode.DISPLAY_WIDTH;
        double tHeight = 2 * DRAW_Y_MARGIN + maxMoves * GameTreeNode.DISPLAY_HEIGHT;
        double componentWidth = getWidth();
        double componentHeight = getHeight();
        // Adjust scrollbar properties depending on component sizing
        boolean vScrollable = componentHeight < tHeight;
        vScroll.setVisible(vScrollable);
        vScroll.setManaged(vScrollable);
        if (vScrollable) {
            vScroll.setMax(tHeight - componentHeight + DRAW_X_MARGIN * 2 + 16);
            vScroll.setVisibleAmount((componentHeight / tHeight) * (tHeight - componentHeight));
        }
        boolean hScrollable = componentWidth < tWidth;
        hScroll.setVisible(hScrollable);
        hScroll.setManaged(hScrollable);
        if (hScrollable) {
            hScroll.setMax(tWidth - componentWidth + DRAW_Y_MARGIN * 2 + 16);
            hScroll.setVisibleAmount((componentWidth / tWidth) * (tWidth - componentWidth));
        }

        corner.setVisible(hScrollable && vScrollable);
        corner.setManaged(hScrollable && vScrollable);
    }

    /**
     * Recursively constructs an entire MoveNode branch as GameTreeNode branch. This method
     * also handles branch collision detection.
     *
     * @param branchRoot The root node of the current branch.
     * @param parent Parent GameTreeNode of the current root node.
     * @param column The intended column the current branch reside (on the UI).
     */
    private void createMoveBranch(GameNode branchRoot, GameTreeNode parent, int column) {
        if (branchRoot.getParent() == null) {
            // This is the root node, initialize
            columnData.putIfAbsent(0, new ArrayList<>());
        }
        GameNode current = branchRoot;
        int size = 0;
        // The branch start is 1 less than the move number to account for collision with the branch outline
        // on the tree UI.
        int start = branchRoot.getMoveNumber() - 1;
        List<GameTreeNode> branchNodes = new ArrayList<>();
        Stack<GameNode> childNodes = new Stack<>();
        Map<GameNode, GameTreeNode> parentNodes = new HashMap<>();
        GameTreeNode currentParent = parent;
        boolean hasChild = false;
        do {
            int moveNumber = current.getMoveNumber();
            double x = DRAW_X_MARGIN + column * GameTreeNode.DISPLAY_WIDTH;
            double y = DRAW_Y_MARGIN + moveNumber * GameTreeNode.DISPLAY_HEIGHT;
            GameTreeNode treeNode = new GameTreeNode(this, currentParent, x, y, current);
            nodeData.putIfAbsent(moveNumber, new ArrayList<>());
            nodeData.get(moveNumber).add(treeNode);
            branchNodes.add(treeNode);
            if (current.hasChildren() && current.getChildren().size() > 1) {
                for (GameNode child : current.getChildren()) {
                    // Ignore the main-variation child because it's already accounted for
                    if (current.getChildren().indexOf(child) == 0)
                        continue;
                    childNodes.push(child);
                    parentNodes.put(child, treeNode);
                }
            }
            currentParent = treeNode;
            size++;
            hasChild = false;
            if (current.equals(currentNode)) {
                currentTreeNode = treeNode;
            }
            if (current.hasChildren()) {
                current = current.getChildren().get(0);
                hasChild = true;
            }
        } while(hasChild);
        int end = start + size;
        int col = column;
        while (!isSegmentAvailable(col, start, end))
            col++;

        columnData.putIfAbsent(col, new ArrayList<>());
        columnData.get(col).add(new Integer[] { start, end });
        if (maxColumns < col)
            maxColumns = col;
        if (maxMoves < start + size)
            maxMoves = start + size;

        for (GameTreeNode node : branchNodes) {
            node.setDisplayColumn(col);
        }

        // Handle child nodes
        while (!childNodes.isEmpty()) {
            GameNode node = childNodes.pop();
            int nthChild = node.getParent().getChildren().indexOf(node);
            createMoveBranch(node, parentNodes.get(node), parentNodes.get(node).getDisplayColumn() + nthChild);
        }
    }

    private void render() {
        canvas.render();
    }

    @Override
    public void gameInitialized(Game game) {
    }

    @Override
    public void gameCurrentMoveUpdate(GameNode currentMove, boolean newMove) {
        // TODO: Find a more efficient way to do this.
        reloadTree();
        updateNodeView();

        this.currentNode = currentMove;
        // Find the current tree node
        int moveNum = currentMove.getMoveNumber();
        List<GameTreeNode> nodes = nodeData.get(moveNum);
        if (nodes != null) {
            for (GameTreeNode node : nodes) {
                if (node.getNode().equals(currentMove)) {
                    currentTreeNode = node;
                    break;
                }
            }
            updateNodeView();
        }
        render();
    }

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

    public GameNode getCurrentMove() {
        return currentNode;
    }

    public double getXOffset() {
        return -xScroll;
    }

    public double getYOffset() {
        return -yScroll;
    }

    public Map<Integer, List<GameTreeNode>> getNodeData() {
        return nodeData;
    }

    public Map<Integer, List<Integer[]>> getColumnData() {
        return columnData;
    }

    private static class GameTreeCanvas extends Canvas {

        private GameTreePane parent;
        private GraphicsContext g;

        private double mouseX = 0, mouseY = 0;
        private boolean focused = false;
        // The current node selected by the cursor
        private GameTreeNode selectedNode = null;

        public GameTreeCanvas(GameTreePane parent) {
            this.g = getGraphicsContext2D();
            this.parent = parent;

            addEventHandler(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
            addEventHandler(MouseEvent.MOUSE_EXITED, this::mouseExited);
            addEventHandler(MouseEvent.MOUSE_MOVED, this::mouseMoved);
            addEventHandler(MouseEvent.MOUSE_CLICKED, this::mouseClicked);
            addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
        }

        private void mouseEntered(MouseEvent evt) {
            requestFocus();
            focused = true;
        }

        private void mouseExited(MouseEvent evt) {
            focused = false;
        }

        private void mouseMoved(MouseEvent evt) {
            this.mouseX = evt.getX();
            this.mouseY = evt.getY();

        }

        private void mouseClicked(MouseEvent evt) {
            this.mouseX = evt.getX();
            this.mouseY = evt.getY();

            double xMargin = GameTreePane.DRAW_X_MARGIN;
            double yMargin = GameTreePane.DRAW_Y_MARGIN;
            double nodeHeight = GameTreeNode.DISPLAY_HEIGHT;
            double nodeWidth = GameTreeNode.DISPLAY_WIDTH;

            int targetY = (int) ((mouseY - yMargin) / nodeHeight);
            if (targetY < 0)
                targetY = 0;
            int targetX = (int) ((mouseX - xMargin) / nodeWidth);

            Map<Integer, List<GameTreeNode>> nodeList = parent.getNodeData();
            List<GameTreeNode> moveVariations = nodeList.get(targetY);
            if (moveVariations == null)
                return;

            if (targetX < moveVariations.size()) {
                GameTreeNode selectedNode = moveVariations.get(targetX);
                // TODO Node selection is still buggy...
            }
        }

        private void mouseDragged(MouseEvent evt) {
            this.mouseX = evt.getX();
            this.mouseY = evt.getY();
        }

        public void render() {
            // TODO: This is hard coded?
            g.setFill(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            //TODO temporary
            // TODO Render only those in visible regions
            for (int moveNum : parent.getNodeData().keySet()) {
                List<GameTreeNode> moveItems = parent.getNodeData().get(moveNum);
                for (GameTreeNode item : moveItems) {
                    item.renderLines(g);
                }
            }

            for (int moveNum : parent.getNodeData().keySet()) {
                List<GameTreeNode> moveItems = parent.getNodeData().get(moveNum);
                for (GameTreeNode item : moveItems) {
                    item.render(g);
                }
            }
        }
    }

    private static class GameTreeNode {
        // Size constants (pixels)
        public static final int DISPLAY_WIDTH = 24;
        public static final int DISPLAY_HEIGHT = 24;

        private static final Color[] STONE_COLORS = { Color.WHITE, Color.BLACK };

        /*
            Display co-ordinates on canvas.
         */
        private double x;
        private double y;
        private int displayRow;
        private GameNode node;
        private GameTreePane treePane;
        private GameTreeNode parent;

        public GameTreeNode(GameTreePane treePane, GameTreeNode parent, double x, double y, GameNode node) {
            this.parent = parent;
            setTreePane(treePane);
            setX(x);
            setY(y);
            setNode(node);
        }

        public void renderLines(GraphicsContext g) {
            double iconSize = Math.min(getWidth() / 2, getHeight() / 2);
            double ox = treePane.getXOffset();
            double oy = treePane.getYOffset();

            // TODO temporary
            if (node.equals(treePane.getCurrentMove())) {
                g.setFill(Color.LIGHTBLUE);
                g.fillOval(getX() + ox, getY() + oy, getWidth(), getHeight());
            }
            if (parent != null) {
                g.setStroke(Color.LIGHTGRAY);
                g.setLineWidth(2d);
            /*
                When drawing connection lines, it's important to keep the lines tidy if one parent node
                has multiple children. A straight line is only drawn for the first two parent-child connections.
                The rest of the lines will be chained through the 2nd child to ensure cleaner presentation.
             */
                if (getDisplayColumn() - parent.getDisplayColumn() > 1) {
                    double x1 = ox + parent.getX() + getWidth() / 2;
                    double y1 = oy + parent.getY() + getHeight() / 2;
                    g.strokeLine(x1, y1, getX() + getWidth() / 2 + ox, y1);
                    g.strokeLine(getX() + getWidth() / 2 + ox, y1, getX() + getWidth() / 2 + ox, getY() + getHeight() / 2 + oy);
                } else {
                    double x1 = ox + parent.getX() + getWidth() / 2;
                    double y1 = oy + parent.getY() + getHeight() / 2;
                    double x2 = ox + getX() + getWidth() / 2;
                    double y2 = oy + getY() + getHeight() / 2;
                    g.strokeLine(x1, y1, x2, y2);
                }
                g.setLineWidth(1d);
            }
        }

        public void render(GraphicsContext g) {
            double iconSize = Math.min(getWidth() / 2, getHeight() / 2);
            // Stone icon
            // TODO Make this more sexy later
            double ox = treePane.getXOffset();
            double oy = treePane.getYOffset();
            double x = ox + getX() + getWidth() / 2 - iconSize / 2;
            double y = oy + getY() + getHeight() / 2 - iconSize / 2;

            if (node.isRootNode()) {
                g.setFill(Color.ORANGE);
                g.fillRect(x, y, iconSize, iconSize);
                g.setStroke(Color.WHITE);
                g.strokeRect(x, y, iconSize, iconSize);
            } else if (node.getColor() == Stone.BLACK
                    || node.getColor() == Stone.WHITE) {
                g.setFill(STONE_COLORS[node.getMoveNumber() % 2]);
                g.fillOval(x, y, iconSize, iconSize);
                g.setStroke(Color.BLACK);
                g.strokeOval(x, y, iconSize, iconSize);
            }
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getWidth() {
            return DISPLAY_WIDTH;
        }

        public double getHeight() {
            return DISPLAY_HEIGHT;
        }

        public int getDisplayColumn() {
            return displayRow;
        }

        public void setDisplayColumn(int column) {
            this.displayRow = column;
            x = GameTreePane.DRAW_X_MARGIN + getDisplayColumn() * DISPLAY_HEIGHT;
        }

        public GameTreeNode getParent() {
            return parent;
        }

        public GameNode getNode() {
            return node;
        }

        public void setNode(GameNode node) {
            this.node = node;
        }

        public GameTreePane getTreePane() {
            return treePane;
        }

        public void setTreePane(GameTreePane treePane) {
            this.treePane = treePane;
        }
    }
}
