package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.go.GoGameModel;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * Represents one rendered content layer on the board.
 */
abstract class GameBoardCanvas extends Canvas {

    private final GraphicsContext graphics;
    protected final GameBoardManager manager;

    GameBoardCanvas(GameBoardManager manager) {
        this.graphics = getGraphicsContext2D();
        this.manager = manager;
    }

    void render(GameBoardManager manager) {
        _render(graphics, manager);
    }

    protected abstract void _render(GraphicsContext g, GameBoardManager manager);

    public abstract void onGameModelSet(GoGameModel model, GameBoardManager manager);

    public abstract void onGameUpdate(GoGameModel game, GameBoardManager manager);
}
