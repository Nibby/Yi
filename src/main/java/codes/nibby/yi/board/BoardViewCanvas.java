package codes.nibby.yi.board;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameNode;
import codes.nibby.yi.game.Markup;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Canvas for rendering elements currently present in the game node.
 * This layer draws:
 * <ul>
 *     <li>Background image/color</li>
 *     <li>Line grid</li>
 *     <li>Co-ordinate labels</li>
 *     <li>Stone shadows</li>
 * </ul>
 * <p>
 *
 * @author Kevin Yang
 * Created on 21 March 2020
 */
public class BoardViewCanvas extends BoardCanvasLayer {

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

    @Override
    protected void _render(GraphicsContext g, Game game, GameBoard board) {
        g.clearRect(0, 0, getWidth(), getHeight());

        // TODO: Allow theme parameter override on these routines
        drawBackground(g, board);
        drawGameBoard(g, board);
        drawStones(g, board, game);
    }

    private void drawStones(GraphicsContext g, GameBoard gameBoard, Game game) {
        final GameNode currentNode = game.getCurrentNode();
        Stone[] stones = gameBoard.getAllRenderableStones();
        Stone currentStone = null;
        int[] currentMove = currentNode.getCurrentMove();
        for (Stone stone : stones) {
            if (stone == null)
                continue;

            StoneRenderer.renderTextureWithShadow(g, stone, gameBoard.getMetrics());
            if (currentMove != null && stone.getX() == currentMove[0] && stone.getY() == currentMove[1])
                currentStone = stone;
        }

        int[] boardData = currentNode.getStoneData();
        currentNode.getMarkups().forEach(markup -> {
            Color color = boardData[markup.getY1() * game.getBoardWidth() + markup.getX1()] == Game.COLOR_BLACK
                    ? Color.WHITE : Color.BLACK;
            MarkupRenderer.render(g, null, markup, gameBoard.getMetrics(), color);
        });

        if (currentStone != null) {
            int[] move = currentNode.getCurrentMove();
            Markup markup = Markup.circle(move[0], move[1]);
            Color markerColor = currentNode.getColor() == Game.COLOR_BLACK ? Color.WHITE : Color.BLACK;
            MarkupRenderer.render(g, currentStone, markup, gameBoard.getMetrics(), markerColor);
        }
    }

    private void drawGameBoard(GraphicsContext g, GameBoard gameBoard) {
        BoardMetrics metrics = gameBoard.getMetrics();
        double gridSize = metrics.getGridSize();
        int boardWidth = metrics.getBoardWidth();
        int boardHeight = metrics.getBoardHeight();
        double offsetX = metrics.getOffsetX();
        double offsetY = metrics.getOffsetY();
        double gridOffsetX = metrics.getGridOffsetX();
        double gridOffsetY = metrics.getGridOffsetY();
        double gap = metrics.getGap();

        double width = gridSize * boardWidth;
        double height = gridSize * boardHeight;
        double x = getWidth() / 2 - width / 2;
        double y = getHeight() / 2 - height / 2;

        g.setEffect(TEXTURE_SHADOW);
        g.setFill(TEXTURE_SHADOW_COLOR);
        g.fillRect(x - TEXTURE_SHADOW_MARGIN, y - TEXTURE_SHADOW_MARGIN,
                width + TEXTURE_SHADOW_MARGIN * 2, height + TEXTURE_SHADOW_MARGIN * 2);
        g.setEffect(null);

        // TODO: Allow theme parameter override
        if (Config.getBoardTheme().shouldDrawBoardTexture()) {
            Image boardTexture = Config.getBoardTheme().getBoardTexture();
            g.drawImage(boardTexture, x, y, width, height);
        }

        if (Config.getBoardTheme().shouldDrawGrid()) {
            // TODO: Allow theme parameter override
            g.setFill(Config.getBoardTheme().getGridColor());
            g.setStroke(Config.getBoardTheme().getGridColor());
            g.setLineWidth(1d);

            // Board lines
            for (int xx = 0; xx < boardWidth; xx++) {
                g.strokeLine(metrics.getGridX(xx), offsetY + gridOffsetY, metrics.getGridX(xx),
                        metrics.getGridY(boardHeight - 1));
            }

            for (int yy = 0; yy < boardHeight; yy++) {
                g.strokeLine(offsetX + gridOffsetX, metrics.getGridY(yy),
                        metrics.getGridX(boardWidth - 1), metrics.getGridY(yy));
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

                double xx = gridOffsetX + offsetX + corner * grid - dotSize / 2;
                double yy = gridOffsetY + offsetY + corner * grid - dotSize / 2;

                double x1 = gridOffsetX + offsetX + (boardWidth - corner - 1) * grid - dotSize / 2;
                double y1 = gridOffsetY + offsetY + (boardHeight - corner - 1) * grid - dotSize / 2;

                g.fillOval(xx, yy, dotSize, dotSize);
                g.fillOval(x1, yy, dotSize, dotSize);
                g.fillOval(xx, y1, dotSize, dotSize);
                g.fillOval(x1, y1, dotSize, dotSize);

                if (boardWidth == 19) {
                    double x2 = gridOffsetX + offsetX + centerDot * grid - dotSize / 2;
                    double y2 = gridOffsetY + offsetY + centerDot * grid - dotSize / 2;

                    g.fillOval(x2, yy, dotSize, dotSize);
                    g.fillOval(x2, y1, dotSize, dotSize);
                    g.fillOval(xx, y2, dotSize, dotSize);
                    g.fillOval(x1, y2, dotSize, dotSize);
                }
            }
        }
    }

    private void drawBackground(GraphicsContext g, GameBoard gameBoard) {
        if (Config.getBoardTheme().shouldDrawBackground()) {
            Image backgroundTexture = Config.getBoardTheme().getBoardBackgroundTexture();
            g.drawImage(backgroundTexture, 0, 0, getWidth(), getHeight());
        }
    }
}
