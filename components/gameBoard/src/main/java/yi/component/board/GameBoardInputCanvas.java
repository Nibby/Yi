package yi.component.board;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import yi.core.go.GameModel;

import java.util.Optional;

/**
 * Handles and manages all keyboard and mouse input to the {@link GameBoardViewer}. Performs rapid repaints of
 * lightweight objects (such as the transparent intersection cursor).
 */
final class GameBoardInputCanvas extends GameBoardCanvas {

    private int cursorX = 0, cursorY = 0;
    private boolean renderCursor = false;

    GameBoardInputCanvas(GameBoardManager manager) {
        super(manager);

        setFocusTraversable(true);

        addEventHandler(MouseEvent.ANY, this::onMouseEvent);
        addEventHandler(KeyEvent.ANY, this::onKeyEvent);
        addEventFilter(ScrollEvent.SCROLL, this::onScrollEvent);
    }

    @Override
    protected void _render(GraphicsContext g, GameBoardManager manager) {
        g.clearRect(0, 0, getWidth(), getHeight());
        
        if (renderCursor) {
            var editMode = manager.edit.getEditMode();
            editMode.getMouseCursor().ifPresent(this::setCursor);
            editMode.renderGridCursor(g, manager, cursorX, cursorY);
        }
    }

    @Override
    public void onGameModelSet(GameModel model, GameBoardManager manager) {

    }

    @Override
    public void onGameUpdate(GameModel game, GameBoardManager manager) {

    }

    private void onMouseEvent(MouseEvent e) {
        if (!manager.edit.isEditable()) {
            return;
        }
        retrieveCursorPosition(e.getX(), e.getY());

        if (renderCursor) {
            if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
                manager.edit.getEditMode().onMousePress(manager, cursorX, cursorY);
            }
        }

        render(manager);
    }

    private void onKeyEvent(KeyEvent e) {
        if (e.getEventType() == KeyEvent.KEY_PRESSED) {
            manager.edit.getEditMode().onKeyPress(manager, e);
        }
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

    public void onScrollEvent(ScrollEvent e) {
        double deltaY = e.getDeltaY();

        if (deltaY < 0) {
            manager.model.toNextMove();
        } else if (deltaY > 0) {
            manager.model.toPreviousMove();
        }
    }

    private void clearCursorPosition() {
        renderCursor = false;
    }

    private void setCursorPosition(int[] gridPosition) {
        renderCursor = true;
        cursorX = gridPosition[0];
        cursorY = gridPosition[1];
    }

    private void retrieveCursorPosition(double mouseX, double mouseY) {
        Optional<int[]> cursorPosition = manager.size.getGridPosition(mouseX, mouseY);
        cursorPosition.ifPresentOrElse(this::setCursorPosition, this::clearCursorPosition);
    }
}
