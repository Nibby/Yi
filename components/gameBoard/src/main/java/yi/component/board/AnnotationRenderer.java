package yi.component.board;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import yi.component.utilities.ShapeUtilities;
import yi.core.go.Annotation;
import yi.core.go.StoneColor;

public final class AnnotationRenderer {

    private AnnotationRenderer() { }

    public static void render(Annotation annotation, GraphicsContext g, GameBoardManager manager) {
        if (annotation instanceof Annotation.PointAnnotation) {
            renderPointAnnotation((Annotation.PointAnnotation) annotation, g, manager);
        } else if (annotation instanceof Annotation.DirectionalAnnotation) {
            renderDirectionalAnnotation((Annotation.DirectionalAnnotation) annotation, g, manager);
        } else {
            unsupportedType(annotation);
        }
    }

    private static void renderPointAnnotation(Annotation.PointAnnotation annotation,
                                              GraphicsContext g, GameBoardManager manager) {
        double stoneSize = manager.size.getStoneSizeInPixels();
        double[] position = manager.size.getStoneRenderPosition(annotation.getX(), annotation.getY());
        double x = position[0];
        double y = position[1];

        Rectangle stoneBounds = new Rectangle(x, y, stoneSize, stoneSize);
        Rectangle annoBounds = ShapeUtilities.clip(stoneBounds, stoneSize / 4d);
        var smallerBounds = ShapeUtilities.clip(annoBounds, stoneSize/24d);

        // TODO: Work out how to color the annotation based on the underlying stone and board texture
        Color color;
        var stone = manager.getGameModel()
                           .getCurrentGameState()
                           .getBoardPosition()
                           .getStoneColorAt(annotation.getX(), annotation.getY());

        if (stone == StoneColor.BLACK) {
            color = Color.WHITE;
        } else {
            color = Color.BLACK;
        }

        double originalLineWidth = g.getLineWidth();
        double lineWidth = getLineWidth(stoneSize);
        g.setLineWidth(lineWidth);
        g.setStroke(color);
        g.setFill(color);

        switch (annotation.getType()) {
            case TRIANGLE:
                renderTriangle(g, annoBounds.getX(), annoBounds.getY(), annoBounds.getWidth());
                break;
            case SQUARE:
                renderSquare(g, smallerBounds.getX(), smallerBounds.getY(), smallerBounds.getWidth());
                break;
            case CIRCLE:
                renderCircle(g, annoBounds.getX(), annoBounds.getY(), annoBounds.getWidth());
                break;
            case CROSS:
                renderCross(g, smallerBounds.getX(), smallerBounds.getY(), smallerBounds.getWidth());
                break;
            case LABEL:
                assert annotation instanceof Annotation.Label;
                renderLabel(g, ((Annotation.Label) annotation).getText(), annoBounds.getX(),
                        annoBounds.getY(), annoBounds.getWidth());
                break;
            case FADE:
                // Stone size + stone gap size, tiles nicely with adjacent fade annotations
                double gridUnitSize = manager.size.getGridUnitSizeInPixels();
                renderFade(g, x, y, gridUnitSize);
                break;
            case _DOT:
                var bounds = ShapeUtilities.clip(annoBounds, annoBounds.getWidth() / 4d);
                renderDot(g, bounds.getX(), bounds.getY(), bounds.getWidth());
                break;
            default:
                unsupportedType(annotation);
                break;
        }

        g.setLineWidth(originalLineWidth);
    }

    private static void renderDirectionalAnnotation(Annotation.DirectionalAnnotation annotation,
                                                    GraphicsContext g, GameBoardManager manager) {
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

    private static void renderLabel(GraphicsContext g, String text, double x, double y, double width) {
        g.fillText(text, x, y, width);
    }

    private static void renderArrow(GraphicsContext g, double xStart, double yStart,
                                    double xEnd, double yEnd, double stoneSize) {
        renderLine(g, xStart, yStart, xEnd, yEnd);

        final double tipSize = stoneSize / 4d;

        final double endMagnitude = Math.sqrt(Math.pow((xEnd - xStart), 2) + Math.pow((yEnd - yStart), 2));
        final double xEndNormal = (xEnd - xStart) / endMagnitude;
        final double yEndNormal = (yEnd - yStart) / endMagnitude;

        final double xStartNormal = 0d;
        final double yStartNormal = 1d;

        final double dotProduct = xStartNormal * xEndNormal + yStartNormal * yEndNormal;
        final double magnitude = Math.sqrt(Math.pow(xStartNormal, 2)
                + Math.pow(yStartNormal, 2)) * Math.sqrt(Math.pow(xEndNormal, 2)
                + Math.pow(yEndNormal, 2));
        double angle = Math.toDegrees(Math.acos(dotProduct / magnitude));

        if (xEndNormal > 0) {
            angle = -angle;
        }

        var transform = g.getTransform();
        g.setTransform(new Affine(new Rotate(angle, xEnd, yEnd)));
        renderLine(g, xEnd, yEnd, xEnd - tipSize, yEnd - tipSize);
        renderLine(g, xEnd, yEnd, xEnd + tipSize, yEnd - tipSize);
        g.setTransform(transform);
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

    private static void renderDot(GraphicsContext g, double x, double y, double size) {
        g.fillOval(x, y, size, size);
    }

    private static double getLineWidth(double stoneSize) {
        return stoneSize / 16d;
    }

    private static void unsupportedType(Annotation annotation) {
        throw new IllegalStateException("Unsupported annotation type: " + annotation.getClass().toString());
    }
}
