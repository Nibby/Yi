package codes.nibby.yi.editor.gui.board;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

abstract class GameBoardCanvas extends Canvas {

    private GraphicsContext graphics;

    GameBoardCanvas() {
        this.graphics = getGraphicsContext2D();
    }

    void render(GameBoardManager manager) {
        _render(graphics, manager);
    }

    protected abstract void _render(GraphicsContext g, GameBoardManager manager);

}
