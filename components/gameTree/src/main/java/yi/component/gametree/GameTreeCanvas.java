package yi.component.gametree;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Collection;

final class GameTreeCanvas extends Canvas {

    private static final int ELEMENT_WIDTH = 20;
    private static final int ELEMENT_HEIGHT = 20;

    private final GraphicsContext graphics;

    public GameTreeCanvas() {
        graphics = getGraphicsContext2D();
    }

    public void render(Collection<TreeElement> visibleElements) {
        graphics.setFill(Color.BLACK);

        for (var element : visibleElements) {
            int x = element.getLogicalX() * ELEMENT_WIDTH;
            int y = element.getLogicalY() * ELEMENT_HEIGHT;

            graphics.fillOval(x + 3, y + 3, ELEMENT_WIDTH - 6, ELEMENT_HEIGHT - 6);
        }
    }
}
