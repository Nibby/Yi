package codes.nibby.yi.board;

import codes.nibby.yi.config.Config;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * The bottom-most layer of the game board canvas stack.
 * This layer draws:
 * <ul>
 *     <li>Background image/color</li>
 *     <li>Line grid</li>
 *     <li>Co-ordinate labels</li>
 *     <li>Stone shadows</li>
 * </ul>
 *
 * Stone shadows are drawn in this layer because of stone placement
 * animations. Stones that are animated will be elevated to the
 * top-most layer (BoardInputCanvas). If shadows are included as
 * part of the stone drawing routine, then there will be shadow
 * overlay issues.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class BoardBackgroundCanvas extends Canvas {

    private static final DropShadow TEXTURE_SHADOW = new DropShadow();
    private static final Color TEXTURE_SHADOW_COLOR = Color.color(0.25d, 0.25d, 0.25d, 0.25d);
    private static final int TEXTURE_SHADOW_MARGIN = 10;

    static {
        // TODO may be temporary, need to scale it with board size
        TEXTURE_SHADOW.setRadius(15);
        TEXTURE_SHADOW.setOffsetX(10);
        TEXTURE_SHADOW.setOffsetY(10);
        TEXTURE_SHADOW.setColor(Color.color(0d, 0d, 0d, 0.5d));
    }

    private GameBoard gameBoard;
    private GraphicsContext g;

    public BoardBackgroundCanvas(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        g = getGraphicsContext2D();
    }

    public void render() {
        BoardMetrics metrics = gameBoard.getMetrics();
        double gridSize = metrics.getGridSize();
        int boardWidth = metrics.getBoardWidth();
        int boardHeight = metrics.getBoardHeight();
        double offsetX = metrics.getOffsetX();
        double offsetY = metrics.getOffsetY();
        double gridOffsetX = metrics.getGridOffsetX();
        double gridOffsetY = metrics.getGridOffsetY();
        double gap = metrics.getGap();

        g.clearRect(0, 0, getWidth(), getHeight());
        {
            // TODO: Allow theme parameter override
            // Draw the backdrop
            if (Config.getBoardTheme().shouldDrawBackground()) {
                Image backgroundTexture = Config.getBoardTheme().getBoardBackgroundTexture();
                g.drawImage(backgroundTexture, 0, 0, getWidth(), getHeight());
            }

            // Draw the board
            double width = gridSize * boardWidth;
            double height = gridSize * boardHeight;
            double x = getWidth() / 2 - width / 2;
            double y = getHeight() / 2 - height / 2;
            if (gameBoard.getTopToolBar() != null)
                y += (double) BoardMetrics.RESERVED_TOOLBAR_SIZE / 2;

            g.setEffect(TEXTURE_SHADOW);
            g.setFill(TEXTURE_SHADOW_COLOR);
            g.fillRect(x - TEXTURE_SHADOW_MARGIN, y - TEXTURE_SHADOW_MARGIN,
                    width + TEXTURE_SHADOW_MARGIN * 2, height + TEXTURE_SHADOW_MARGIN * 2);
            g.setEffect(null);
            // TODO: Allow theme parameter override
            // Draw board texture
            if (Config.getBoardTheme().shouldDrawBoardTexture()) {
                Image boardTexture = Config.getBoardTheme().getBoardTexture();
                g.drawImage(boardTexture, x, y, width, height);
            }
        }

        if (Config.getBoardTheme().shouldDrawGrid()) {
            // TODO: Allow theme parameter override
            g.setFill(Config.getBoardTheme().getGridColor());
            g.setStroke(Config.getBoardTheme().getGridColor());

            // Board lines
            for (int x = 0; x < boardWidth; x++) {
                g.strokeLine(metrics.getGridX(x),offsetY + gridOffsetY, metrics.getGridX(x),
                        metrics.getGridY(boardHeight - 1));
            }

            for (int y = 0; y < boardHeight; y++) {
                g.strokeLine(offsetX + gridOffsetX, metrics.getGridY(y),
                        metrics.getGridX(boardWidth - 1), metrics.getGridY(y));
            }

            // Board star points
            double dotSize = gridSize / 6;
            int centerDot = (boardWidth % 2 == 1) ? (boardWidth - 1) / 2 : -1;

            if (boardWidth == boardHeight && (boardWidth == 9 || boardWidth == 13 || boardWidth == 19)) {
                int corner = boardWidth == 9 ? 2 : 3;
                double grid = gridSize - gap;

                if (centerDot != -1)
                    g.fillOval(metrics.getGridX(centerDot) - dotSize / 2,
                            metrics.getGridY(centerDot) - dotSize / 2, dotSize, dotSize);

                double x = gridOffsetX + offsetX + corner * grid - dotSize / 2;
                double y = gridOffsetY + offsetY + corner * grid - dotSize / 2;

                double x1 = gridOffsetX + offsetX + (boardWidth - corner - 1) * grid - dotSize / 2;
                double y1 = gridOffsetY + offsetY + (boardHeight - corner - 1) * grid - dotSize / 2;

                g.fillOval(x, y, dotSize, dotSize);
                g.fillOval(x1, y, dotSize, dotSize);
                g.fillOval(x, y1, dotSize, dotSize);
                g.fillOval(x1, y1, dotSize, dotSize);

                if (boardWidth == 19) {
                    double x2 = gridOffsetX + offsetX + centerDot * grid - dotSize / 2;
                    double y2 = gridOffsetY + offsetY + centerDot * grid - dotSize / 2;

                    g.fillOval(x2, y, dotSize, dotSize);
                    g.fillOval(x2, y1, dotSize, dotSize);
                    g.fillOval(x, y2, dotSize, dotSize);
                    g.fillOval(x1, y2, dotSize, dotSize);
                }
            }
        }

        if (Config.getBoardTheme().shouldDrawStoneShadow()) {
            boolean redraw = false;
            Stone[] stones = gameBoard.getAllRenderableStones();
            for (Stone stone : stones) {
                if (stone == null)
                    continue;

                StoneRenderer.renderShadow(g, stone, metrics);
                if (stone.shouldWobble())
                    redraw = true;
            }

            if (redraw) {
                new Timeline(new KeyFrame(Duration.millis(40), e -> render())).play();
            }
        }
    }
}
