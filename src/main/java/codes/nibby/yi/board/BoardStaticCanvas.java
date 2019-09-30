package codes.nibby.yi.board;

import codes.nibby.yi.game.Markup;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * The middle layer of the canvas stack.
 * This layer draws all still, permanent objects on the game board.
 * These are primarily stones and annotations.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class BoardStaticCanvas extends Canvas {

    private GameBoard gameBoard;
    private GraphicsContext g;

    public BoardStaticCanvas(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        g = getGraphicsContext2D();
    }

    public void render() {
        g.clearRect(0,0, getWidth(), getHeight());

        List<Stone> staticStones = gameBoard.getStaticStones();
        for (Stone stone : staticStones) {
            StoneRenderer.renderTexture(g, stone, gameBoard.getMetrics());
        }

        // TODO testing code, remove later.
        MarkupRenderer.render(g, null, Markup.triangle(0,0), gameBoard.getMetrics(), Color.BLACK);
        MarkupRenderer.render(g, null, Markup.circle(1,0), gameBoard.getMetrics(), Color.BLACK);
        MarkupRenderer.render(g, null, Markup.square(2,0), gameBoard.getMetrics(), Color.BLACK);
        MarkupRenderer.render(g, null, Markup.cross(3,0), gameBoard.getMetrics(), Color.BLACK);
    }
}
