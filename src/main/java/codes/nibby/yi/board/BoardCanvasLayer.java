package codes.nibby.yi.board;

import codes.nibby.yi.game.Game;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public abstract class BoardCanvasLayer extends Canvas {

    private final GraphicsContext g;

    BoardCanvasLayer() {
        this.g = getGraphicsContext2D();
    }

    protected abstract void _render(GraphicsContext g, Game game, GameBoard gameBoard);

    public void render(Game game, GameBoard gameBoard) {
        _render(g, game, gameBoard);
    }

}
