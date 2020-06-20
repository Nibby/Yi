package codes.nibby.yi.gui.board;

import codes.nibby.yi.settings.Settings;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

final class GameBoardGameCanvas extends GameBoardCanvas {

    @Override
    protected void _render(GraphicsContext g, GameBoardManager manager) {
        g.clearRect(0, 0, getWidth(), getHeight());

        renderBackground(g, manager);
        renderBoard(g, manager);
        renderStones(g, manager);
    }

    private void renderBackground(GraphicsContext g, GameBoardManager manager) {


        Rectangle boardBounds = manager.boardSize.getBoardBounds();
        g.drawImage(Settings.boardTheme.getBackgroundImage(), boardBounds.getX(), boardBounds.getY(), boardBounds.getWidth(), boardBounds.getHeight());
    }

    private void renderBoard(GraphicsContext g, GameBoardManager manager) {

    }

    private void renderStones(GraphicsContext g, GameBoardManager manager) {

    }
}
