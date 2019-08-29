package codes.nibby.yi.board;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.game.Game;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;

/**
 * The top-most layer of the board canvas stack.
 * This layer handles:
 * <ul>
 *     <li>Peripheral input, propagates them to GameBoard parent</li>
 *     <li>Rendering cursor indicators</li>
 *     <li>Rendering animated stones (where contiguous redraw is required)</li>
 * </ul>
 */
public class BoardInputCanvas extends Canvas {

    private static final Color COLOR_BLACK = new Color(0d, 0d, 0d, 0.85d);
    private static final Color COLOR_WHITE = new Color(1d, 1d, 1d, 0.85d);

    private GameBoard gameBoard;
    private GraphicsContext g;

    private int mouseX = -1, mouseY = -1;
    private int lastMouseX = -1, lastMouseY = -1;

    public BoardInputCanvas(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        g = getGraphicsContext2D();

        addEventHandler(MouseEvent.MOUSE_MOVED, this::mouseMoved);
        addEventHandler(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
        addEventHandler(MouseEvent.MOUSE_EXITED, this::mouseExited);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
        addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        addEventHandler(ScrollEvent.SCROLL, this::mouseScrolled);

        addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);
        addEventHandler(KeyEvent.KEY_RELEASED, this::keyReleased);

    }

    public void render() {
        g.clearRect(0, 0, getWidth(), getHeight());

        // Draw all animated stones.
        List<Stone> animatedStones = gameBoard.getAnimatedStones();
        boolean redraw = false;
        for (Stone stone : animatedStones) {
            stone.wobble();
            StoneRenderer.renderTexture(g, stone, gameBoard.getMetrics());

            if (stone.shouldWobble())
                redraw = true;
        }

        // Draw board cursor
        if (mouseX >= 0 && mouseY >= 0) {
            BoardCursorType cursorType = Config.getCursorType();
            BoardMetrics metrics = gameBoard.getMetrics();
            double w = metrics.getStoneSize() / 2;
            double h = metrics.getStoneSize() / 2;
            double x = metrics.getBoardStoneX(mouseX) + w / 2;
            double y = metrics.getBoardStoneY(mouseY) + h / 2;
            int nextColor = gameBoard.getGame().getNextMoveColor();
            Stone[] stones = gameBoard.getAllRenderableStones();
            Stone hover = stones[mouseX + mouseY * gameBoard.getGame().getBoardWidth()];
            if (hover != null) {
                x += hover.getFuzzyX() + hover.getWobbleX();
                y += hover.getFuzzyY() + hover.getWobbleY();
            }

            if (nextColor == Game.COLOR_BLACK)
                g.setFill(COLOR_BLACK);
            else
                g.setFill(COLOR_WHITE);

            switch (cursorType) {
                case CIRCLE:
                    g.fillOval(x, y, w, h);
                    break;
                case SQUARE:
                    g.fillRect(x, y, w, h);
                    break;
                case STONE:
                    // TODO: Implement later
                    break;
            }
        }

        if (redraw) {
            new Timeline(new KeyFrame(Duration.millis(40), e -> render())).play();
        }
    }

    private void mouseMoved(MouseEvent evt) {
        requestFocus();
        updateMousePosition(evt);
        if (gameBoard.getController() != null)
            gameBoard.getController().mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
        render();
    }

    private void mouseEntered(MouseEvent evt) {
        requestFocus();
        updateMousePosition(evt);
        if (gameBoard.getController() != null)
            gameBoard.getController().mouseEntered();
        render();
    }

    private void mouseExited(MouseEvent evt) {
        mouseX = -1;
        mouseY = -1;
        lastMouseX = -1;
        lastMouseY = -1;
        if (gameBoard.getController() != null)
            gameBoard.getController().mouseExited();
        render();
    }

    private void mouseDragged(MouseEvent evt) {
        requestFocus();
        updateMousePosition(evt);
        if (gameBoard.getController() != null)
            gameBoard.getController().mouseDragged(mouseX, mouseY, lastMouseX, lastMouseY, evt.getButton());
        render();
    }

    private void mousePressed(MouseEvent evt) {
        requestFocus();
        updateMousePosition(evt);
        if (gameBoard.getController() != null)
            gameBoard.getController().mousePressed(mouseX, mouseY, lastMouseX, lastMouseY, evt.getButton());
        render();
    }

    private void mouseReleased(MouseEvent evt) {
        requestFocus();
        updateMousePosition(evt);
        if (gameBoard.getController() != null)
            gameBoard.getController().mouseReleased(mouseX, mouseY, lastMouseX, lastMouseY, evt.getButton());
        render();
    }

    private void mouseScrolled(ScrollEvent evt) {
        if (gameBoard.getController() != null)
            gameBoard.getController().mouseScrolled(evt.getDeltaY());
    }

    private void keyPressed(KeyEvent evt) {
        if (gameBoard.getController() != null)
            gameBoard.getController().keyPressed(evt.getCode());
    }

    private void keyReleased(KeyEvent evt) {
        if (gameBoard.getController() != null)
            gameBoard.getController().keyReleased(evt.getCode());
    }

    /**
     * Calculates the mouse position from a set of board metrics.
     *
     * @param evt Mouse event.
     */
    private void updateMousePosition(MouseEvent evt) {
        BoardMetrics metrics = gameBoard.getMetrics();
        double offsetX = metrics.getOffsetX();
        double offsetY = metrics.getOffsetY();
        double gridOffsetX = metrics.getGridOffsetX();
        double gridOffsetY = metrics.getGridOffsetY();
        double gridSize = metrics.getGridSize() - metrics.getGap();

        double mx = evt.getX() - offsetX - gridOffsetX + gridSize / 3;
        double my = evt.getY() - offsetY - gridOffsetY + gridSize / 3;

        mouseX = (int) (mx / gridSize);
        mouseY = (int) (my / gridSize);

        if (mouseX < 0 || mouseY < 0 || mouseX > metrics.getBoardWidth() - 1
                || mouseY > metrics.getBoardHeight() - 1) {
            mouseX = -1;
            mouseY = -1;
            return;
        }

        if (lastMouseX != mouseX || lastMouseY != mouseY) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
    }
}
