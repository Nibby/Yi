package yi.component.gametree;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import yi.core.common.GameNode;
import yi.core.go.GoGameStateUpdate;

import java.util.Collection;
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

    public void render(Camera camera, Collection<TreeElement> visibleElements, GameNode<GoGameStateUpdate> currentNode, GameTreeElementSize size) {
        graphics.clearRect(0, 0, getWidth(), getHeight());
        graphics.setStroke(Color.BLACK);
        graphics.setLineWidth(2d);
        graphics.setFill(Color.BLACK);

        double offsetX = camera.getOffsetX();
        double offsetY = camera.getOffsetY();

        Collection<TreeNodeElement> nodeElements = visibleElements.stream()
                .filter(element -> element instanceof TreeNodeElement)
                .map(nodeElement -> (TreeNodeElement) nodeElement)
                .collect(Collectors.toList());

        final double gridWidth = size.getGridSize().getWidth();
        final double gridHeight = size.getGridSize().getHeight();

        renderTracks(nodeElements, gridWidth, gridHeight, offsetX, offsetY);
        renderNodes(nodeElements, currentNode, gridWidth, gridHeight, offsetX, offsetY);
    }

    private void renderNodes(Collection<TreeNodeElement> nodeElements, GameNode<GoGameStateUpdate> currentNode, double gridWidth, double gridHeight, double offsetX, double offsetY) {
        for (var nodeElement : nodeElements) {
            double x = nodeElement.getGridX() * gridWidth + offsetX;
            double y = nodeElement.getGridY() * gridHeight + offsetY;

            graphics.setFill(Color.BLACK);

            if (nodeElement.isHighlighted()) {
                graphics.setFill(Color.GRAY);
            }

            if (nodeElement.getNode().equals(currentNode)) {
                graphics.setFill(Color.BLUE);
            }

            graphics.fillOval(x + 3, y + 3, gridWidth - 6, gridHeight - 6);
        }
    }

    private void renderTracks(Collection<TreeNodeElement> nodeElements, double gridWidth, double gridHeight, double offsetX, double offsetY) {
        for (var nodeElement : nodeElements) {
            nodeElement.getParent().ifPresent(parent -> {
                double px = parent.getGridX() * gridWidth;
                double  py = parent.getGridY() * gridHeight;

                double pCenterX = px + gridWidth / 2d + offsetX;
                double pCenterY = py + gridHeight / 2d + offsetY;

                double  x = nodeElement.getGridX() * gridWidth;
                double  y = nodeElement.getGridY() * gridHeight;

                double centerX = x + gridWidth / 2d + offsetX;
                double centerY = y + gridHeight / 2d + offsetY;

                graphics.strokeLine(pCenterX, pCenterY, centerX, pCenterY);
                graphics.strokeLine(centerX, pCenterY, centerX, centerY);
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
