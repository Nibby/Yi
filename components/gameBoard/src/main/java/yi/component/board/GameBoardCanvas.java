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

    protected void render(GameBoardManager manager) {
        renderImpl(graphics, manager);
    }

    protected abstract void renderImpl(GraphicsContext g, GameBoardManager manager);

    public abstract void onGameModelSet(GameModel newModel, GameBoardManager manager);

    public abstract void onGameUpdate(GameModel gameModel, GameBoardManager manager);
}
