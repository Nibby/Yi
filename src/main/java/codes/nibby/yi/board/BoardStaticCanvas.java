package codes.nibby.yi.board;

import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameNode;
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

    static int i = 0;
    public void render() {
        g.clearRect(0, 0, getWidth(), getHeight());

        Game game = gameBoard.getGame();
        final GameNode currentNode = game.getCurrentNode();
        List<Stone> staticStones = gameBoard.getStaticStones();
        Stone currentStone = null;
        int[] currentMove = currentNode.getCurrentMove();
        for (Stone stone : staticStones) {
            StoneRenderer.renderTexture(g, stone, gameBoard.getMetrics());
            if (currentMove != null && stone.getX() == currentMove[0] && stone.getY() == currentMove[1])
                currentStone = stone;
        }

        int[] boardData = currentNode.getStoneData();
        currentNode.getMarkups().forEach(markup -> {
            Color color = boardData[markup.getY1() * game.getBoardWidth() + markup.getX1()] == Game.COLOR_BLACK
                    ? Color.WHITE : Color.BLACK;
            MarkupRenderer.render(g, null, markup, gameBoard.getMetrics(), color);
        });

        if (currentStone != null) {
            int[] move = currentNode.getCurrentMove();
            Markup markup = Markup.circle(move[0], move[1]);
            Color markerColor = currentNode.getColor() == Game.COLOR_BLACK ? Color.WHITE : Color.BLACK;
            MarkupRenderer.render(g, currentStone, markup, gameBoard.getMetrics(), markerColor);
        }
    }
}
