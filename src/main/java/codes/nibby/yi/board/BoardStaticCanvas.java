package codes.nibby.yi.board;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

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
    }
}
