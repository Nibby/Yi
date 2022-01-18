package yi.component.boardviewer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import yi.core.go.GameModel;

/**
 * A {@code GameBoardCanvas} is a drawable canvas component that makes up the
 * game board. The {@link GameBoardViewer} presents the game board using a stack
 * of canvases, each rendering its own aspect of the board.
 */
abstract class GameBoardCanvas extends Canvas {

    private final GraphicsContext graphics;

    /**
     * Board manager shared by all game board canvases.
     */
    protected final GameBoardManager manager;

    GameBoardCanvas(GameBoardManager manager) {
        this.graphics = getGraphicsContext2D();
        this.manager = manager;
    }

    protected void render(GameBoardManager manager) {
        renderImpl(graphics, manager);
    }

    protected abstract void renderImpl(GraphicsContext g, GameBoardManager manager);

    /**
     * Called when a new go game has been loaded. This will redraw the canvas
     * using game data from the given {@link GameModel}.
     *
     * @param newModel New game model to show.
     * @param manager Board manager.
     */
    public abstract void onGameModelSet(GameModel newModel, GameBoardManager manager);

    /**
     * Called when the current game model has been changed. This may trigger
     * redraw if something has changed since the last redraw was done.
     *
     * @param gameModel Game model that was changed.
     * @param manager Board manager.
     */
    public abstract void onGameUpdate(GameModel gameModel, GameBoardManager manager);
}
