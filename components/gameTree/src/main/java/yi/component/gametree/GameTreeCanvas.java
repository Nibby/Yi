package yi.component.gametree;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import yi.core.common.GameNode;
import yi.core.go.GoGameStateUpdate;

import java.util.Collection;
import java.util.stream.Collectors;

final class GameTreeCanvas extends Canvas {

    private final GraphicsContext graphics;

    public GameTreeCanvas() {
        graphics = getGraphicsContext2D();
    }

    public void render(Camera camera, Collection<TreeElement> visibleElements, GameNode<GoGameStateUpdate> currentNode, GameTreeElementSize size) {
        graphics.clearRect(0, 0, getWidth(), getHeight());
        graphics.setStroke(Color.BLACK);
        graphics.setLineWidth(2d);
        graphics.setFill(Color.BLACK);

        double offsetX = camera.getOffsetX();
        double offsetY = camera.getOffsetY();

        Collection<TreeNodeElement> nodes = visibleElements.stream()
                .filter(element -> element instanceof TreeNodeElement)
                .map(nodeElement -> (TreeNodeElement) nodeElement)
                .collect(Collectors.toList());

        final double ELEMENT_WIDTH = size.getGridSize().getWidth();
        final double ELEMENT_HEIGHT = size.getGridSize().getHeight();

        for (var element : nodes) {
            element.getParent().ifPresent(parent -> {
                double px = parent.getLogicalX() * ELEMENT_WIDTH;
                double  py = parent.getLogicalY() * ELEMENT_HEIGHT;

                double pCenterX = px + ELEMENT_WIDTH / 2d + offsetX;
                double pCenterY = py + ELEMENT_HEIGHT / 2d + offsetY;

                double  x = element.getLogicalX() * ELEMENT_WIDTH;
                double  y = element.getLogicalY() * ELEMENT_HEIGHT;

                double centerX = x + ELEMENT_WIDTH / 2d + offsetX;
                double centerY = y + ELEMENT_HEIGHT / 2d + offsetY;

                graphics.strokeLine(pCenterX, pCenterY, centerX, pCenterY);
                graphics.strokeLine(centerX, pCenterY, centerX, centerY);
            });
        }

        for (var element : visibleElements) {
            double x = element.getLogicalX() * ELEMENT_WIDTH + offsetX;
            double y = element.getLogicalY() * ELEMENT_HEIGHT + offsetY;

            graphics.setFill(Color.BLACK);
            if (element instanceof TreeNodeElement) {
                var node = ((TreeNodeElement) element).getNode();
                if (node.equals(currentNode)) {
                    graphics.setFill(Color.BLUE);
                }
            }
            graphics.fillOval(x + 3, y + 3, ELEMENT_WIDTH - 6, ELEMENT_HEIGHT - 6);
        }
    }
}
