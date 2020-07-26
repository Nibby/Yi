package yi.component.board;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import yi.core.go.GameModel;

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

    public abstract void onGameModelSet(GameModel model, GameBoardManager manager);

    public abstract void onGameUpdate(GameModel game, GameBoardManager manager);
}
