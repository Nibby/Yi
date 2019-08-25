package codes.nibby.yi.board;

import codes.nibby.yi.Yi;
import codes.nibby.yi.config.Config;
import codes.nibby.yi.utility.AlertUtility;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class StoneRenderer {

    private static DropShadow shadow;

    static {
        shadow = new DropShadow();
        shadow.setBlurType(BlurType.GAUSSIAN);
        shadow.setColor(Color.color(0.15f, 0.15f, 0.15f, 0.5f));
    }

    public static void drawStone(GraphicsContext g, Stone stone, BoardMetrics metrics, double x, double y) {
        StoneStyle stoneStyle = Config.getStoneStyle();
        if (stoneStyle == null) {
            // TODO: Make this more user friendly
            AlertUtility.showAlert("Unrecognized stone style.", "StoneRenderer Error",
                    Alert.AlertType.ERROR, ButtonType.OK);
            Yi.exit();
            return;
        }

        switch (stoneStyle) {
            case BICONVEX_CERAMIC:
                CeramicStone.draw(g, stone, metrics.getStoneSize(), x, y);
                break;
            default:
                throw new RuntimeException("StoneStyle not implemented: " + Config.getStoneStyle().name());
        }
    }

    public static void drawShadow(GraphicsContext g, Stone stone, BoardMetrics metrics, double x, double y) {
        double size = metrics.getStoneSize();
        shadow.setRadius(size / 8);
        shadow.setOffsetX(size / 12);
        shadow.setOffsetY(size / 12);
    }

    private static class CeramicStone {

        static RadialGradient gradientWhite, gradientBlack;
        static final float rgFocusAngle = 250f;
        static final float rgFocusDistance = 0.1f;
        static final float  rgCenterX = 0.35f;
        static final float  rgCenterY = 0.35f;
        static final float rgRadius = 0.45f;

        static {
            shadow = new DropShadow();
            shadow.setBlurType(BlurType.GAUSSIAN);
            shadow.setColor(Color.color(0.15f, 0.15f, 0.15f, 0.5f));

            gradientBlack = new RadialGradient(rgFocusAngle, rgFocusDistance, rgCenterX, rgCenterY, rgRadius,
                    true, CycleMethod.NO_CYCLE,
                    new Stop(0d, Color.color(0.45d, 0.45d, 0.45d, 1d)),
                    new Stop(0.99d, Color.color(0.1d, 0.1d, 0.1d, 1d)));

            gradientWhite = new RadialGradient(rgFocusAngle, rgFocusDistance, rgCenterX, rgCenterY, rgRadius,
                    true, CycleMethod.NO_CYCLE,
                    new Stop(0d, Color.color(1d, 249d/255d, 235d/255d, 1d)),
                    new Stop(0.99d, Color.color(230d/255d, 224d/255d, 211d/255d, 1d)));

        }

        public static void draw(GraphicsContext g, Stone stone, double size, double x, double y) {
            if (stone.getColor() == Stone.BLACK) {
                g.setFill(gradientBlack);
            } else if (stone.getColor() == Stone.WHITE) {
                g.setFill(gradientWhite);
            }
            g.fillOval(x, y, size, size);
        }
    }
}
