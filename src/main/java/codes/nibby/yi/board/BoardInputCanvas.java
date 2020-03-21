package codes.nibby.yi.board;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.Markup;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

import java.lang.ref.WeakReference;

/**
 * Canvas for rendering user input prompts and cursors.
 *
 * @author Kevin Yang
 * Created on 21 March 2020
 */
public class BoardInputCanvas extends BoardCanvasLayer {

    private static final Color COLOR_BLACK = new Color(0d, 0d, 0d, 0.85d);
    private static final Color COLOR_WHITE = new Color(1d, 1d, 1d, 0.85d);

    private int mouseX = -1, mouseY = -1;
    private int lastMouseX = -1, lastMouseY = -1;
    private GameBoardController controller;

    private WeakReference<Game> cachedGame;
    private WeakReference<GameBoard> cachedGameBoard;

    public BoardInputCanvas(GameBoard gameBoard, GameBoardController controller) {
        this.controller = controller;
        this.cachedGameBoard = new WeakReference<>(gameBoard);

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

    @Override
    protected void _render(GraphicsContext g, Game game, GameBoard gameBoard) {
        cachedGame = new WeakReference<>(game);
        cachedGameBoard = new WeakReference<>(gameBoard);

        g.clearRect(0, 0, getWidth(), getHeight());
        boolean drawCursor = mouseX >= 0 && mouseY >= 0 && !game.getCurrentNode().hasMarkupAt(mouseX, mouseY, false);

        if (drawCursor) {
            drawCursor(g, game, gameBoard);
        }
    }

    private void drawCursor(GraphicsContext g, Game game, GameBoard gameBoard) {
        BoardCursorType cursorType = Config.getCursorType();
        BoardMetrics metrics = gameBoard.getMetrics();
        double w = metrics.getStoneSize() / 2;
        double h = metrics.getStoneSize() / 2;
        double x = metrics.getBoardStoneX(mouseX) + w / 2;
        double y = metrics.getBoardStoneY(mouseY) + h / 2;
        int nextColor = game.getNextMoveColor();
        Stone[] stones = gameBoard.getAllRenderableStones();
        Stone hover = stones[mouseX + mouseY * game.getBoardWidth()];
        Color markupColor = hover != null && hover.getColor() == Game.COLOR_BLACK ? Color.WHITE : Color.BLACK;

        g.setGlobalAlpha(0.65d);
        BoardInputHintType inputHint = gameBoard.getInputHint();
        if (inputHint.equals(BoardInputHintType.DYNAMIC)) {
            if (hover == null) {
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
                        StoneRenderer.renderTexture(g, nextColor, metrics.getStoneSize(), metrics.getBoardStoneX(mouseX), metrics.getBoardStoneY(mouseY));
                        break;
                }
            }
        } else if (inputHint.equals(BoardInputHintType.STONE_BLACK)) {
            StoneRenderer.renderTexture(g, Game.COLOR_BLACK, metrics.getStoneSize(), metrics.getBoardStoneX(mouseX), metrics.getBoardStoneY(mouseY));
        } else if (inputHint.equals(BoardInputHintType.STONE_WHITE)) {
            StoneRenderer.renderTexture(g, Game.COLOR_WHITE, metrics.getStoneSize(), metrics.getBoardStoneX(mouseX), metrics.getBoardStoneY(mouseY));
        } else if (inputHint.equals(BoardInputHintType.MARKUP_TRIANGLE)) {
            MarkupRenderer.render(g, hover, Markup.triangle(mouseX, mouseY), metrics, markupColor);
        } else if (inputHint.equals(BoardInputHintType.MARKUP_SQUARE)) {
            MarkupRenderer.render(g, hover, Markup.square(mouseX, mouseY), metrics, markupColor);
        } else if (inputHint.equals(BoardInputHintType.MARKUP_CIRCLE)) {
            MarkupRenderer.render(g, hover, Markup.circle(mouseX, mouseY), metrics, markupColor);
        } else if (inputHint.equals(BoardInputHintType.MARKUP_CROSS)) {
            MarkupRenderer.render(g, hover, Markup.cross(mouseX, mouseY), metrics, markupColor);
        } else if (inputHint.equals(BoardInputHintType.MARKUP_LABEL)) {
            // TODO implement later
        }

        g.setGlobalAlpha(1.0d);
    }

    private void mouseMoved(MouseEvent evt) {
        requestFocus();
        updateMousePosition(evt);
        if (controller != null)
            controller.mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
        redrawIfPossible();
    }

    private void mouseEntered(MouseEvent evt) {
        requestFocus();
        updateMousePosition(evt);
        if (controller != null)
            controller.mouseEntered();
        redrawIfPossible();
    }

    private void mouseExited(MouseEvent evt) {
        mouseX = -1;
        mouseY = -1;
        lastMouseX = -1;
        lastMouseY = -1;
        if (controller != null)
            controller.mouseExited();
        redrawIfPossible();
    }

    private void mouseDragged(MouseEvent evt) {
        requestFocus();
        updateMousePosition(evt);
        if (controller != null)
            controller.mouseDragged(mouseX, mouseY, lastMouseX, lastMouseY, evt.getButton());
        redrawIfPossible();
    }

    private void mousePressed(MouseEvent evt) {
        requestFocus();
        updateMousePosition(evt);
        if (controller != null)
            controller.mousePressed(mouseX, mouseY, lastMouseX, lastMouseY, evt.getButton());
        redrawIfPossible();
    }

    private void mouseReleased(MouseEvent evt) {
        requestFocus();
        updateMousePosition(evt);
        if (controller != null)
            controller.mouseReleased(mouseX, mouseY, lastMouseX, lastMouseY, evt.getButton());
        redrawIfPossible();
    }

    private void mouseScrolled(ScrollEvent evt) {
        if (controller != null)
            controller.mouseScrolled(evt.getDeltaY());
        redrawIfPossible();
    }

    private void keyPressed(KeyEvent evt) {
        if (controller != null)
            controller.keyPressed(evt.getCode());
        redrawIfPossible();
    }

    private void keyReleased(KeyEvent evt) {
        if (controller != null)
            controller.keyReleased(evt.getCode());
        redrawIfPossible();
    }

    /**
     * Calculates the mouse position from a set of board metrics.
     *
     * @param evt Mouse event.
     */
    private void updateMousePosition(MouseEvent evt) {
        GameBoard gameBoard = cachedGameBoard.get();
        if (gameBoard == null)
            return;

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

    private void redrawIfPossible() {
        if (cachedGame != null && cachedGameBoard != null) {
            render(cachedGame.get(), cachedGameBoard.get());
        }
    }

    public void setController(GameBoardController controller) {
        this.controller = controller;
    }
}
