package yi.component.gametree;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import yi.core.go.GameNode;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the rendering of the game tree.
 *
 * See {@link GameTreeViewer.CanvasInputHandler} for input handling aspects of the canvas.
 */
final class GameTreeCanvas extends Canvas {

    private final GraphicsContext graphics;

    public GameTreeCanvas() {
        graphics = getGraphicsContext2D();
    }

    public void addInputHandler(InputHandler handler) {
        addEventHandler(MouseEvent.MOUSE_MOVED, handler::mouseMoved);
        addEventHandler(MouseEvent.MOUSE_PRESSED, handler::mousePressed);
        addEventHandler(MouseEvent.MOUSE_CLICKED, handler::mouseClicked);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, handler::mouseDragged);
        addEventHandler(ScrollEvent.SCROLL, handler::mouseScrolled);
        addEventHandler(KeyEvent.KEY_PRESSED, handler::keyPressed);
    }

    public void render(Camera camera, List<TreeElement> visibleElements, GameNode currentNode, List<GameNode> currentVariationHistory, GameTreeElementSize size) {
        graphics.clearRect(0, 0, getWidth(), getHeight());
        graphics.setFill(GameTreeColors.BACKGROUND);
        graphics.fillRect(0, 0, getWidth(), getHeight());

        graphics.setStroke(GameTreeColors.NODE);
        graphics.setLineWidth(2d);
        graphics.setFill(GameTreeColors.NODE);

        double offsetX = camera.getOffsetX();
        double offsetY = camera.getOffsetY();

        List<TreeNodeElement> nodeElements = visibleElements.stream()
                .filter(element -> element instanceof TreeNodeElement)
                .map(nodeElement -> (TreeNodeElement) nodeElement)
                .collect(Collectors.toList());

        final double gridWidth = size.getGridSize().getWidth();
        final double gridHeight = size.getGridSize().getHeight();

        renderTracks(nodeElements, currentVariationHistory, gridWidth, gridHeight, offsetX, offsetY);
        renderNodes(nodeElements, currentNode, currentVariationHistory, gridWidth, gridHeight, offsetX, offsetY);
    }

    private void renderNodes(List<TreeNodeElement> nodeElements, GameNode currentNode, List<GameNode> currentVariationHistory, double gridWidth, double gridHeight, double offsetX, double offsetY) {
        for (var nodeElement : nodeElements) {
            double x = nodeElement.getGridX() * gridWidth + offsetX;
            double y = nodeElement.getGridY() * gridHeight + offsetY;

            graphics.setFill(GameTreeColors.NODE);

            if (nodeElement.isHighlighted()) {
                graphics.setFill(GameTreeColors.NODE_HOVER);
            }

            var node = nodeElement.getNode();

            if (node.equals(currentNode)) {
                graphics.setFill(GameTreeColors.CURRENT_NODE);
            } else if (currentVariationHistory.contains(node)) {
                graphics.setFill(GameTreeColors.NODE_IN_CURRENT_VARIATION);
            }

            var bounds = new Rectangle(x + 3, y + 3, gridWidth - 6, gridHeight - 6);

            if (node.isRoot()) {
                graphics.fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
            } else {
                graphics.fillOval(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
            }
        }
    }

    private void renderTracks(List<TreeNodeElement> nodeElements, List<GameNode> currentVariationHistory, double gridWidth, double gridHeight, double offsetX, double offsetY) {
        for (int i = nodeElements.size() - 1; i > 0; --i) {
            var nodeElement = nodeElements.get(i);

            nodeElement.getParent().ifPresent(parent -> {
                double px = parent.getGridX() * gridWidth;
                double py = parent.getGridY() * gridHeight;

                double pCenterX = px + gridWidth / 2d + offsetX;
                double pCenterY = py + gridHeight / 2d + offsetY;

                double x = nodeElement.getGridX() * gridWidth;
                double y = nodeElement.getGridY() * gridHeight;

                double centerX = x + gridWidth / 2d + offsetX;
                double centerY = y + gridHeight / 2d + offsetY;

                var parentNode = parent.getNode();
                var thisNode = nodeElement.getNode();

                if (currentVariationHistory.contains(thisNode)) {
                    graphics.setStroke(GameTreeColors.NODE_IN_CURRENT_VARIATION);
                } else {
                    graphics.setStroke(GameTreeColors.NODE);
                }

                // No need to re-draw the top branch line since the last child draws it first.
                // This way we also avoid node branches that are not the current variation drawing over the
                // current variation branch color.
                boolean drawBranchLine = false;

                if (currentVariationHistory.contains(parentNode)) {
                    var parentIndexInHistory = currentVariationHistory.indexOf(parentNode);
                    var continuationNode = parentIndexInHistory + 1 < currentVariationHistory.size() ? currentVariationHistory.get(parentIndexInHistory + 1) : null;

                    if (continuationNode != null) {
                        assert parentNode.hasChild(continuationNode);

                        var continuationOrder = parentNode.getChildOrder(continuationNode);
                        var thisOrder = parentNode.getChildOrder(thisNode);

                        if (thisOrder >= continuationOrder) {
                            drawBranchLine = true;
                        }
                    } else {
                        drawBranchLine = true;
                    }
                } else {
                    drawBranchLine = true;
                }

                if (drawBranchLine) {
                    graphics.strokeLine(pCenterX, pCenterY, centerX, pCenterY);
                }

                graphics.strokeLine(centerX, pCenterY+2, centerX, centerY);
            });
        }
    }

    /**
     * A set of listeners to receive canvas input events.
     */
    interface InputHandler {

        void mouseMoved(MouseEvent e);
        void mousePressed(MouseEvent e);
        void mouseClicked(MouseEvent e);
        void mouseDragged(MouseEvent e);
        void mouseScrolled(ScrollEvent e);

        void keyPressed(KeyEvent e);

    }
}
