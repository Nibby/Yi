package yi.component.gametree;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import yi.core.common.GameNode;
import yi.core.go.GoGameStateUpdate;

import java.util.Collection;
import java.util.stream.Collectors;

final class GameTreeCanvas extends Canvas {

    private static final int ELEMENT_WIDTH = 20;
    private static final int ELEMENT_HEIGHT = 20;

    private final GraphicsContext graphics;

    public GameTreeCanvas() {
        graphics = getGraphicsContext2D();
    }

    public void render(Collection<TreeElement> visibleElements, GameNode<GoGameStateUpdate> currentNode) {
        graphics.clearRect(0, 0, getWidth(), getHeight());
        graphics.setStroke(Color.BLACK);
        graphics.setLineWidth(2d);
        graphics.setFill(Color.BLACK);

        Collection<TreeNodeElement> nodes = visibleElements.stream()
                .filter(element -> element instanceof TreeNodeElement)
                .map(nodeElement -> (TreeNodeElement) nodeElement)
                .collect(Collectors.toList());

        for (var element : nodes) {
            element.getParent().ifPresent(parent -> {
                int px = parent.getLogicalX() * ELEMENT_WIDTH;
                int py = parent.getLogicalY() * ELEMENT_HEIGHT;

                double pCenterX = px + ELEMENT_WIDTH / 2d;
                double pCenterY = py + ELEMENT_HEIGHT / 2d;

                int x = element.getLogicalX() * ELEMENT_WIDTH;
                int y = element.getLogicalY() * ELEMENT_HEIGHT;

                double centerX = x + ELEMENT_WIDTH / 2d;
                double centerY = y + ELEMENT_HEIGHT / 2d;

                graphics.strokeLine(pCenterX, pCenterY, centerX, pCenterY);
                graphics.strokeLine(centerX, pCenterY, centerX, centerY);
            });
        }

        for (var element : visibleElements) {
            int x = element.getLogicalX() * ELEMENT_WIDTH;
            int y = element.getLogicalY() * ELEMENT_HEIGHT;

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
