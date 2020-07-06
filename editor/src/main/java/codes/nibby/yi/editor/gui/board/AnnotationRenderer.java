package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.go.Annotation;
import codes.nibby.yi.go.GoStoneColor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public final class AnnotationRenderer {

    private AnnotationRenderer() { }

    public static void render(Annotation annotation, GoStoneColor stone, GraphicsContext g, GameBoardManager manager) {

        // TODO: Work out how to color the annotation based on the underlying stone and board texture

        if (annotation instanceof Annotation.PointAnnotation) {
            double pointAnnotationSize = manager.size.getPointAnnotationSizeInPixels();
            double[] position = manager.size.getPointAnnotationRenderPosition(annotation.getX(), annotation.getY());
            double x = position[0];
            double y = position[1];

            switch (annotation.getType()) {
                case TRIANGLE:
                    renderTriangle(g, x, y, pointAnnotationSize);
                    break;
                case SQUARE:
                    renderSquare(g, x, y, pointAnnotationSize);
                    break;
                case CIRCLE:
                    renderCircle(g, x, y, pointAnnotationSize);
                    break;
                case CROSS:
                    renderCross(g, x, y, pointAnnotationSize);
                    break;
                case FADE:
                    renderFade(g, x, y, pointAnnotationSize);
                    break;
                default:
                    unsupportedType(annotation);
                    break;
            }
        } else if (annotation instanceof Annotation.DirectionalAnnotation) {
            var directional = (Annotation.DirectionalAnnotation) annotation;
            double stoneSize = manager.size.getStoneSizeInPixels();
            double[] start = manager.size.getGridRenderPosition(directional.getX(), directional.getY(), 0d);
            double[] end = manager.size.getGridRenderPosition(directional.getXEnd(), directional.getYEnd(), 0d);

            double xStart = start[0];
            double yStart = start[1];
            double xEnd = end[0];
            double yEnd = end[1];

            switch (annotation.getType()) {
                case LINE:
                    renderLine(g, xStart, yStart, xEnd, yEnd, stoneSize);
                    break;
                case ARROW:
                    renderArrow(g, xStart, yStart, xEnd, yEnd, stoneSize);
                    break;
                default:
                    unsupportedType(annotation);
                    break;
            }

        } else {
            unsupportedType(annotation);
        }
    }

    private static void renderArrow(GraphicsContext g, double xStart, double yStart, double xEnd, double yEnd, double stoneSize) {

    }

    private static void renderLine(GraphicsContext g, double xStart, double yStart, double xEnd, double yEnd, double stoneSize) {

    }

    private static void renderFade(GraphicsContext g, double x, double y, double annotationSize) {

    }

    private static void renderCross(GraphicsContext g, double x, double y, double annotationSize) {

    }

    private static void renderCircle(GraphicsContext g, double x, double y, double annotationSize) {

    }

    private static void renderSquare(GraphicsContext g, double x, double y, double annotationSize) {
        
    }

    private static void renderTriangle(GraphicsContext g, double x, double y, double annotationSize) {

    }

    private static void unsupportedType(Annotation annotation) {
        throw new IllegalStateException("Unsupported annotation type: " + annotation.getClass().toString());
    }
}
