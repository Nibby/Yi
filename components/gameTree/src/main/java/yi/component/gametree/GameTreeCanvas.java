package yi.component.gametree;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Rectangle;
import yi.core.go.GameNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the rendering of the game tree.
 *
 * See {@link GameTreeViewer.CanvasInputHandler} for input handling aspects of the canvas.
 */
final class GameTreeCanvas extends Canvas {

    private static final double BRANCH_LINE_WIDTH = 2d;

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

    public void render(GameTreeViewerSettings settings, Camera camera,
                       List<TreeNodeElement> visibleElements, GameNode currentNode,
                       GameTreeElementSize size) {
        var currentVariationHistory = currentNode.getMoveHistory();

        graphics.clearRect(0, 0, getWidth(), getHeight());
        graphics.setFill(settings.getBackgroundColor());
        graphics.fillRect(0, 0, getWidth(), getHeight());

        graphics.setStroke(settings.getNodeColor());
        graphics.setLineWidth(BRANCH_LINE_WIDTH);
        graphics.setFill(settings.getNodeColor());

        double offsetX = camera.getOffsetX();
        double offsetY = camera.getOffsetY();

        final double gridWidth = size.getGridSize().getWidth();
        final double gridHeight = size.getGridSize().getHeight();

        renderTracks(settings, visibleElements, currentVariationHistory, gridWidth, gridHeight, offsetX, offsetY);
        renderNodes(settings, visibleElements, currentNode, currentVariationHistory, gridWidth, gridHeight, offsetX, offsetY);
    }

    private void renderNodes(GameTreeViewerSettings settings, List<TreeNodeElement> nodeElements,
                             GameNode currentNode, List<GameNode> currentVariationHistory,
                             double gridWidth, double gridHeight, double offsetX, double offsetY) {
        for (var nodeElement : nodeElements) {
            double x = nodeElement.getGridX() * gridWidth + offsetX;
            double y = nodeElement.getGridY() * gridHeight + offsetY;

            graphics.setFill(settings.getNodeColor());

            if (nodeElement.isHighlighted()) {
                graphics.setFill(settings.getNodeHoverColor());
            }

            var node = nodeElement.getNode();

            if (node.equals(currentNode)) {
                graphics.setFill(settings.getCurrentNodeColor());
            } else if (currentVariationHistory.contains(node)) {
                graphics.setFill(settings.getNodeInCurrentVariationColor());
            }

            var bounds = new Rectangle(x + 3, y + 3, gridWidth - 6, gridHeight - 6);

            if (node.isRoot()) {
                graphics.fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
            } else {
                graphics.fillOval(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
            }
        }
    }

    private void renderTracks(GameTreeViewerSettings settings, List<TreeNodeElement> nodeElements, List<GameNode> currentVariationHistory,
                              double gridWidth, double gridHeight, double offsetX, double offsetY) {
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
                    graphics.setStroke(settings.getNodeInCurrentVariationColor());
                } else {
                    graphics.setStroke(settings.getNodeColor());
                }

                // No need to re-draw the top branch line since the last child draws it first.
                // This way we also avoid node branches that are not the current variation drawing over the
                // current variation branch color.
                boolean drawBranchLine = false;

                if (currentVariationHistory.contains(parentNode)) {
                    var parentIndexInHistory = currentVariationHistory.indexOf(parentNode);
                    var continuationNode = parentIndexInHistory + 1 < currentVariationHistory.size() ? currentVariationHistory.get(parentIndexInHistory + 1) : null;

                    if (continuationNode != null) {
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

                graphics.strokeLine(centerX, pCenterY+BRANCH_LINE_WIDTH, centerX, centerY);
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
