package yi.component.board;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import yi.component.common.ShapeUtilities;
import yi.core.go.GoAnnotation;
import yi.core.go.GoStoneColor;

public final class AnnotationRenderer {

    private AnnotationRenderer() { }

    public static void render(GoAnnotation annotation, GraphicsContext g, GameBoardManager manager) {

        if (annotation instanceof GoAnnotation.PointAnnotation) {
            renderPointAnnotation((GoAnnotation.PointAnnotation) annotation, g, manager);
        } else if (annotation instanceof GoAnnotation.DirectionalAnnotation) {
            renderDirectionalAnnotation((GoAnnotation.DirectionalAnnotation) annotation, g, manager);
        } else {
            unsupportedType(annotation);
        }
    }

    private static void renderPointAnnotation(GoAnnotation.PointAnnotation annotation, GraphicsContext g, GameBoardManager manager) {
        double stoneSize = manager.size.getStoneSizeInPixels();
        double[] position = manager.size.getStoneRenderPosition(annotation.getX(), annotation.getY());
        double x = position[0];
        double y = position[1];

        Rectangle stoneBounds = new Rectangle(x, y, stoneSize, stoneSize);
        Rectangle annoBounds = ShapeUtilities.clip(stoneBounds, stoneSize / 4d);

        // TODO: Work out how to color the annotation based on the underlying stone and board texture
        Color color;
        var stone = manager.model.getCurrentGamePosition().getStoneColorAt(annotation.getX(), annotation.getY());

        if (stone == GoStoneColor.BLACK) {
            color = Color.WHITE;
        } else {
            color = Color.BLACK;
        }

        double originalLineWidth = g.getLineWidth();
        double lineWidth = getLineWidth(stoneSize);
        g.setLineWidth(lineWidth);
        g.setStroke(color);

        switch (annotation.getType()) {
            case TRIANGLE:
                renderTriangle(g, annoBounds.getX(), annoBounds.getY(), annoBounds.getWidth());
                break;
            case SQUARE:
                renderSquare(g, annoBounds.getX(), annoBounds.getY(), annoBounds.getWidth());
                break;
            case CIRCLE:
                // Use slightly bigger radius so that it appears roughly the same size as a square
                var bigBounds = ShapeUtilities.clip(stoneBounds, stoneSize / 5d);
                renderCircle(g, bigBounds.getX(), bigBounds.getY(), bigBounds.getWidth());
                break;
            case CROSS:
                renderCross(g, annoBounds.getX(), annoBounds.getY(), annoBounds.getWidth());
                break;
            case FADE:
                // Stone size + stone gap size, tiles nicely with adjacent fade annotations
                double gridUnitSize = manager.size.getGridUnitSizeInPixels();
                renderFade(g, x, y, gridUnitSize);
                break;
            default:
                unsupportedType(annotation);
                break;
        }

        g.setLineWidth(originalLineWidth);
    }

    private static void renderDirectionalAnnotation(GoAnnotation.DirectionalAnnotation annotation, GraphicsContext g, GameBoardManager manager) {
        double stoneSize = manager.size.getStoneSizeInPixels();
        double[] start = manager.size.getGridRenderPosition(annotation.getX(), annotation.getY(), 0d);
        double[] end = manager.size.getGridRenderPosition(annotation.getXEnd(), annotation.getYEnd(), 0d);

        double xStart = start[0];
        double yStart = start[1];
        double xEnd = end[0];
        double yEnd = end[1];

        // TODO: Work out how to color the annotation based on the underlying stone and board texture
        //       Also deal with the optical illusion that white-on-black appears lot larger than black-on-white
        Color color = Color.BLACK;
        g.setStroke(color);

        double lineWidth = getLineWidth(stoneSize) * 2;
        g.setLineWidth(lineWidth);

        switch (annotation.getType()) {
            case LINE:
                renderLine(g, xStart, yStart, xEnd, yEnd);
                break;
            case ARROW:
                renderArrow(g, xStart, yStart, xEnd, yEnd, stoneSize);
                break;
            default:
                unsupportedType(annotation);
                break;
        }
    }

    private static void renderArrow(GraphicsContext g, double xStart, double yStart, double xEnd, double yEnd, double stoneSize) {
        double tipSize = stoneSize / 4d;

        renderLine(g, xStart, yStart, xEnd, yEnd);
        renderLine(g, xEnd, yEnd, xEnd - tipSize, yStart - tipSize);
        renderLine(g, xEnd, yEnd, xEnd - tipSize, yStart + tipSize);
    }

    private static void renderLine(GraphicsContext g, double xStart, double yStart, double xEnd, double yEnd) {
        g.strokeLine(xStart, yStart, xEnd, yEnd);
    }

    private static void renderFade(GraphicsContext g, double x, double y, double size) {
        g.setFill(new Color(0d, 0d, 0d, 0.25d));
        g.fillRect(x, y, size, size);
    }

    private static void renderCross(GraphicsContext g, double x, double y, double size) {
        g.strokeLine(x, y, x+size, y+size);
        g.strokeLine(x+size, y, x, y+size);
    }

    private static void renderCircle(GraphicsContext g, double x, double y, double size) {
        g.strokeOval(x, y, size, size);
    }

    private static void renderSquare(GraphicsContext g, double x, double y, double size) {
        g.strokeRect(x, y, size, size);
    }
    
    private static void renderTriangle(GraphicsContext g, double x, double y, double size) {
        // Looks better when it's not 1:1 ratio
        double centerX = x + size / 2;
        double centerY = y + size / 5 * 2;

        // Lists (x,y) points in a clockwise fashion, starting at 12 o'clock
        var xPoints = new double[] {
            centerX,
            centerX + size / 2,
            centerX - size / 2
        };

        var yPoints = new double[] {
            centerY - size / 5 * 2,
            centerY + size / 2,
            centerY + size / 2,
        };

        g.strokePolygon(xPoints, yPoints, 3);
    }

    private static double getLineWidth(double stoneSize) {
        return stoneSize / 16d;
    }

    private static void unsupportedType(GoAnnotation annotation) {
        throw new IllegalStateException("Unsupported annotation type: " + annotation.getClass().toString());
    }
}
