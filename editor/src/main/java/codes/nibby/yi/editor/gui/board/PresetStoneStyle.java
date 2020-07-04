package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.go.GoStoneColor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public enum PresetStoneStyle {

    CERAMIC_BICONVEX {
        protected double rgFocusAngle = 250d;
        protected double rgFocusDistance = 0.1d;
        protected double rgCenterX = 0.35d;
        protected double rgCenterY = 0.35d;
        protected double rgRadius = 0.45d;

        final RadialGradient blackGradient = createRadialGradient(new Stop(0d, Color.color(0.45d, 0.45d, 0.45d, 1d)), new Stop(0.99d, Color.color(0.1d, 0.1d, 0.0d, 1d)));
        final RadialGradient whiteGradient = createRadialGradient(new Stop(0d, Color.color(0.92d, 0.92d, 0.92d, 1d)), new Stop(0.99d, Color.color(0.85d, 0.85d, 0.85d, 1d)));

        DropShadow dropShadow = null;

        @Override
        public void renderWhite(GraphicsContext g, GameBoardManager manager, double x, double y, double size) {
            updateDropShadowIfNecessary(manager);

            g.setFill(whiteGradient);
            g.setEffect(dropShadow);
            g.fillOval(x, y, size, size);
            g.setEffect(null);
        }

        @Override
        public void renderBlack(GraphicsContext g, GameBoardManager manager, double x, double y, double size) {
            updateDropShadowIfNecessary(manager);

            g.setFill(blackGradient);
            g.setEffect(dropShadow);
            g.fillOval(x, y, size, size);
            g.setEffect(null);
        }

        private void updateDropShadowIfNecessary(GameBoardManager manager) {
            if (dropShadow == null) {
                dropShadow = new DropShadow();
                dropShadow.setBlurType(BlurType.GAUSSIAN);
                dropShadow.setColor(Color.color(0.15f, 0.15f, 0.15f, 0.5f));
            }

            double radius = manager.size.getStoneShadowRadius();
            double offset = manager.size.getStoneShadowOffset();

            dropShadow.setRadius(radius);
            dropShadow.setOffsetX(offset);
            dropShadow.setOffsetY(offset);
        }

        private RadialGradient createRadialGradient(Stop ... stops) {
            return new RadialGradient(rgFocusAngle, rgFocusDistance, rgCenterX, rgCenterY, rgRadius, true, CycleMethod.NO_CYCLE, stops);
        }
    };

    public abstract void renderWhite(GraphicsContext g, GameBoardManager manager, double x, double y, double size);
    public abstract void renderBlack(GraphicsContext g, GameBoardManager manager, double x, double y, double size);

    public void render(GraphicsContext g, GameBoardManager manager, GoStoneColor color, int gridX, int gridY) {
        double stoneSize = manager.size.getStoneSizeInPixels();
        double[] position = manager.size.getStoneRenderPosition(gridX, gridY);
        double x = position[0];
        double y = position[1];

        if (color == GoStoneColor.BLACK) {
            renderBlack(g, manager, x, y, stoneSize);
        } else if (color == GoStoneColor.WHITE) {
            renderWhite(g, manager, x, y, stoneSize);
        }
    }

    public static PresetStoneStyle getDefaultValue() {
        return CERAMIC_BICONVEX;
    }
}
