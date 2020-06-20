package codes.nibby.yi.gui.board;

import codes.nibby.yi.settings.Settings;
import javafx.scene.canvas.GraphicsContext;

final class GameBoardGameCanvas extends GameBoardCanvas {

    @Override
    protected void _render(GraphicsContext g, GameBoardManager manager) {
        renderBackground(g, manager);
        renderBoard(g, manager);
        renderStones(g, manager);
    }

    private void renderBackground(GraphicsContext g, GameBoardManager manager) {
        g.drawImage(Settings.boardTheme.getBackgroundImage(), 0, 0, 400, 300);
    }

    private void renderBoard(GraphicsContext g, GameBoardManager manager) {

    }

    private void renderStones(GraphicsContext g, GameBoardManager manager) {

    }
}
