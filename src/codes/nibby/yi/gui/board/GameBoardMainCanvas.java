package codes.nibby.yi.gui.board;

import codes.nibby.yi.settings.Settings;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Draws the primary contents of the game board, such as the board image, grids, star points,
 * stones and annotations that are part of the game state. In principle, any object that isn't
 * expected to be repainted very quickly should be drawn here.
 *
 * For quick-repaint objects, use {@link GameBoardInputCanvas}.
 */
final class GameBoardMainCanvas extends GameBoardCanvas {

    private static final DropShadow BOARD_BORDER_SHADOW = new DropShadow();
    private static final Color BOARD_BORDER_COLOR = Color.color(0.25d, 0.25d, 0.25d, 0.25d);

    @Override
    protected void _render(GraphicsContext g, GameBoardManager manager) {
        g.clearRect(0, 0, getWidth(), getHeight());

        renderBoard(g, manager);
        renderStones(g, manager);
    }

    private void renderBoard(GraphicsContext g, GameBoardManager manager) {
        BOARD_BORDER_SHADOW.setRadius(15);
        BOARD_BORDER_SHADOW.setOffsetX(10);
        BOARD_BORDER_SHADOW.setOffsetY(10);
        BOARD_BORDER_SHADOW.setColor(Color.color(0d, 0d, 0d, 0.5d));
        g.setEffect(BOARD_BORDER_SHADOW);
        g.setFill(BOARD_BORDER_COLOR);

        Rectangle borderBounds = manager.sizes.getBoardBorderBounds();
        g.fillRect(borderBounds.getX(), borderBounds.getY(), borderBounds.getWidth(), borderBounds.getHeight());
        g.setEffect(null);

        Rectangle boardBounds = manager.sizes.getBoardBounds();
        g.drawImage(Settings.boardTheme.getBoardImage(), boardBounds.getX(), boardBounds.getY(), boardBounds.getWidth(), boardBounds.getHeight());


    }

    private void renderStones(GraphicsContext g, GameBoardManager manager) {

    }
}
