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

/**
 * Provides methods to draw stone textures and shadows.
 * See <strong>StoneStyles</strong> for a list of internally
 * supported stone textures.
 *
 * @author Kevin Yang
 * Created on 25 August 2019
 */
public class StoneRenderer {

    private static DropShadow shadow;

    static {
        // Stone shadows aren't unique to each stone instance.
        shadow = new DropShadow();
        shadow.setBlurType(BlurType.GAUSSIAN);
        shadow.setColor(Color.color(0.15f, 0.15f, 0.15f, 0.5f));
    }

    /**
     * Draws a stone texture onto a go board with stone wobble/fuzzy parameters.
     *
     * @param g       Board graphics context.
     * @param stone   Stone instance.
     * @param metrics Board metrics.
     */
    public static void renderTexture(GraphicsContext g, Stone stone, BoardMetrics metrics) {
        int stoneColor = stone.getColor();
        double wobbleX = stone.getWobbleX();
        double wobbleY = stone.getWobbleY();
        double margin = metrics.getStoneSize() / 20;
        double fuzzyX = stone.getFuzzyX() * margin;
        double fuzzyY = stone.getFuzzyY() * margin;

        double drawX = metrics.getBoardStoneX(stone.getX()) + wobbleX + fuzzyX;
        double drawY = metrics.getBoardStoneY(stone.getY()) + wobbleY + fuzzyY;
        double stoneSize = metrics.getStoneSize();

        renderTexture(g, stoneColor, stoneSize, drawX, drawY);
    }

    /**
     * Draws a stone texture of custom color, size and position.
     *
     * @param g     Canvas graphics context.
     * @param color Color of the Go stone.
     * @param size  Radius (in pixels) of the Go stone.
     * @param x     X draw position.
     * @param y     Y draw position.
     */
    public static void renderTexture(GraphicsContext g, int color, double size, double x, double y) {
        StoneStyle stoneStyle = Config.getStoneStyle();

        if (stoneStyle == null) {
            // TODO: Make this more user friendly, and localised
            AlertUtility.showAlert("Unrecognized stone style.", "StoneRenderer Error",
                    Alert.AlertType.ERROR, ButtonType.OK);
            Yi.exit();
            return;
        }

        switch (stoneStyle) {
            case BICONVEX_CERAMIC:
                CeramicStone.draw(g, color, size, x, y);
                break;
            // TODO: implement more stone styles here.
            default:
                throw new RuntimeException("StoneStyle not implemented: " + Config.getStoneStyle().name());
        }
    }

    public static void renderShadow(GraphicsContext g, Stone stone, BoardMetrics metrics) {
        double wobbleX = stone.getWobbleX();
        double wobbleY = stone.getWobbleY();
        double margin = metrics.getStoneSize() / 20;
        double fuzzyX = stone.getFuzzyX() * margin;
        double fuzzyY = stone.getFuzzyY() * margin;

        double drawX = metrics.getBoardStoneX(stone.getX()) + wobbleX + fuzzyX;
        double drawY = metrics.getBoardStoneY(stone.getY()) + wobbleY + fuzzyY;
        renderShadow(g, metrics, drawX, drawY);
    }

    /**
     * Draws stone shadow at a custom position on the go board.
     *
     * @param g       Canvas graphics context.
     * @param metrics Board metrics.
     * @param x       X draw position.
     * @param y       Y draw position.
     */
    public static void renderShadow(GraphicsContext g, BoardMetrics metrics, double x, double y) {
        double size = metrics.getStoneSize();
        renderShadow(g, size, x, y);
    }

    /**
     * Draws a stone shadow of custom size on a custom canvas.
     *
     * @param g    Canvas graphics context.
     * @param size Shadow size (equal to stone size).
     * @param x    X draw position.
     * @param y    Y draw position.
     */
    public static void renderShadow(GraphicsContext g, double size, double x, double y) {
        shadow.setRadius(size / 8);
        shadow.setOffsetX(size / 12);
        shadow.setOffsetY(size / 12);
        g.setEffect(shadow);
        g.fillOval(x, y, size, size);
        g.setEffect(null);
    }

    /**
     * Draws a fully rendered Go stone (texture + shadow) on one layer.
     *
     * @param g     Canvas graphics context.
     * @param color Color of the go stone. Stone.BLACK | Stone.WHITE
     * @param size  Radius (in pixels) of the Go stone.
     * @param x     X draw position.
     * @param y     Y draw position.
     */
    public static void renderTextureAndShadow(GraphicsContext g, int color, double size, double x, double y) {
        renderShadow(g, size, x, y);
        renderTexture(g, color, size, x, y);
    }

    /**
     * The first, and default, stone texture displayed, if it is not
     * overridden by the current theme.
     */
    private static class CeramicStone {

        static final float rgFocusAngle = 250f;
        static final float rgFocusDistance = 0.1f;
        static final float rgCenterX = 0.35f;
        static final float rgCenterY = 0.35f;
        static final float rgRadius = 0.45f;
        static RadialGradient gradientWhite, gradientBlack;

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
                    new Stop(0d, Color.color(1d, 249d / 255d, 235d / 255d, 1d)),
                    new Stop(0.99d, Color.color(230d / 255d, 224d / 255d, 211d / 255d, 1d)));

        }

        public static void draw(GraphicsContext g, int color, double size, double x, double y) {
            if (color == Stone.BLACK) {
                g.setFill(gradientBlack);
            } else if (color == Stone.WHITE) {
                g.setFill(gradientWhite);
            }
            g.fillOval(x, y, size, size);
        }
    }
}
