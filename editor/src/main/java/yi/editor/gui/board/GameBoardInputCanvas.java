package yi.editor.gui.board;

import yi.editor.gui.board.edits.EditMode;
import yi.core.GoGameModel;
import javafx.event.Event;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.Optional;

/**
 * Handles and manages all keyboard and mouse input to the {@link GameBoard}. Performs rapid repaints of
 * lightweight objects (such as the transparent intersection cursor).
 */
final class GameBoardInputCanvas extends GameBoardCanvas {

    private int cursorX = 0, cursorY = 0;
    private boolean renderCursor = false;

    private EditMode editMode = EditMode.PLAY_MOVE;

    GameBoardInputCanvas(GameBoardManager manager) {
        super(manager);

        addEventHandler(MouseEvent.ANY, this::onMouseEvent);
        addEventHandler(KeyEvent.ANY, this::onKeyEvent);
    }

    @Override
    protected void _render(GraphicsContext g, GameBoardManager manager) {
        g.clearRect(0, 0, getWidth(), getHeight());
        
        if (renderCursor) {
            editMode.getMouseCursor().ifPresent(this::setCursor);
            editMode.renderGridCursor(g, manager, cursorX, cursorY);
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
        retrieveCursorPosition(e.getX(), e.getY());

        if (renderCursor) {
            if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
                editMode.onMousePress(manager, cursorX, cursorY);
            }
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
        retrieveCursorPosition(e.getX(), e.getY());
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
        renderCursor = false;
    }

    private void setCursorPosition(int[] logicalPosition) {
        renderCursor = true;
        cursorX = logicalPosition[0];
        cursorY = logicalPosition[1];
    }

    private void retrieveCursorPosition(double mouseX, double mouseY) {
        Optional<int[]> cursorPosition = manager.size.getGridLogicalPosition(mouseX, mouseY);
        cursorPosition.ifPresentOrElse(this::setCursorPosition, this::clearCursorPosition);
    }
}
