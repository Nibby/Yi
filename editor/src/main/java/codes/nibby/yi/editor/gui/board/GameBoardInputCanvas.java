package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.go.GoGameModel;
import javafx.event.Event;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.Optional;

/**
 * Handles and manages all keyboard and mouse input to the {@link GameBoard}. Performs rapid repaints of
 * lightweight objects (such as the transparent intersection cursor).
 */
final class GameBoardInputCanvas extends GameBoardCanvas {

    private int cursorX = 0, cursorY = 0;
    private boolean drawCursor = false;

    GameBoardInputCanvas(GameBoardManager manager) {
        super(manager);

        addEventHandler(MouseEvent.ANY, this::onMouseEvent);
        addEventHandler(KeyEvent.ANY, this::onKeyEvent);
    }

    @Override
    protected void _render(GraphicsContext g, GameBoardManager manager) {
        g.clearRect(0, 0, getWidth(), getHeight());
        
        if (drawCursor) {
            g.setStroke(Color.BLACK);
            double stoneSize = manager.size.getStoneSizeInPixels();
            double[] position = manager.size.getGridRenderPosition(cursorX, cursorY, stoneSize);
            g.fillOval(position[0], position[1], stoneSize, stoneSize);
        }
    }

    @Override
    public void onGameModelSet(GoGameModel model, GameBoardManager manager) {

    }

    @Override
    public void onGameUpdate(GoGameModel game, GameBoardManager manager) {

    }

    private void onMouseEvent(MouseEvent e) {
        if (!manager.edit.isEditable()) {
            return;
        }

        if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
            onMouseMove(e);
        }

        render(manager);
    }

    private <GenericKeyEvent extends Event> void onKeyEvent(GenericKeyEvent t) {

    }

    public void onKeyPress(KeyEvent e) {

    }

    public void onKeyRelease(KeyEvent e) {

    }

    public void onKeyType(KeyEvent e) {

    }

    public void onMouseMove(MouseEvent e) {
        Optional<int[]> cursorPosition = manager.size.getGridLogicalPosition(e.getX(), e.getY());
        cursorPosition.ifPresentOrElse(this::setCursorPosition, this::clearCursorPosition);
    }

    public void onMouseClick(MouseEvent e) {

    }

    public void onMouseDrag(MouseEvent e) {

    }

    public void onMousePress(MouseEvent e) {

    }

    public void onMouseRelease(MouseEvent e) {

    }

    public void onMouseEnter(MouseEvent e) {

    }

    public void onMouseExit(MouseEvent e) {
        clearCursorPosition();
    }

    private void clearCursorPosition() {
        drawCursor = false;
    }

    private void setCursorPosition(int[] logicalPosition) {
        drawCursor = true;
        cursorX = logicalPosition[0];
        cursorY = logicalPosition[1];
    }
}
