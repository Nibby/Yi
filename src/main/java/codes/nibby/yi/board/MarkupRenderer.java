package codes.nibby.yi.board;

import codes.nibby.yi.game.Markup;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class MarkupRenderer {

    public static void render(GraphicsContext g, Stone stone, Markup markup, BoardMetrics metrics, Color color) {
        g.setFill(color);
        g.setStroke(color);
        double originalLineWidth = g.getLineWidth();
        g.setLineWidth(2d);

        int x1 = markup.getX1();
        int y1 = markup.getY1();

        double x = metrics.getBoardStoneX(x1);
        double y = metrics.getBoardStoneY(y1);
        double gridSize = metrics.getGridSize();


        switch (markup.getType()) {
            case TRIANGLE:
                g.strokePolygon(
                        new double[]{x + gridSize / 2, x + gridSize / 4, x + gridSize / 4 * 3},
                        new double[]{y + gridSize / 4, y + gridSize / 8 * 5, y + gridSize / 8 * 5}, 3);
                break;
            case SQUARE:
                g.strokeRect(x + gridSize / 7 * 2, y + gridSize / 7 * 2, gridSize / 7 * 3, gridSize / 7 * 3);
                break;
            case CIRCLE:
                g.strokeOval(x + gridSize / 4, y + gridSize / 4, gridSize / 2, gridSize / 2);
                break;
            case CROSS:
                g.strokeLine(x + gridSize / 4, y + gridSize / 4, x + gridSize / 4 * 3, y + gridSize / 4 * 3);
                g.strokeLine(x + gridSize / 4, y + gridSize / 4 * 3, x + gridSize / 4 * 3, y + gridSize / 4);
                break;
            case LABEL:
                g.setFill(Color.color(1d, 1d, 1d, 0.1d));
                g.fillOval(x, y, gridSize, gridSize);

                g.setFill(color);
                g.setStroke(color);

                Text t = new Text(markup.getArguments());
                t.applyCss();
                double width = t.getLayoutBounds().getWidth();
                double height = t.getLayoutBounds().getHeight();
                g.fillText(markup.getArguments(), x + gridSize / 2 - width / 2, y + gridSize / 2 + height / 4);

                break;
            case LINE:
                // TODO implement later
                break;
            case ARROW:
                // TODO implement later
                break;
        }
        g.setLineWidth(originalLineWidth);
    }

}
