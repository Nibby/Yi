package yi.component.board.editmodes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import yi.component.board.GameBoardManager;
import yi.core.go.StoneColor;

public final class StoneEditMode extends AbstractEditMode {

    private StoneColor stoneColor;

    public StoneEditMode(StoneColor stoneColor) {
        this.stoneColor = stoneColor;
    }
    
    @Override
    public void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY) {

    }

    @Override
    public void onMousePress(MouseButton button, GameBoardManager manager, int gridX, int gridY) {

    }

    @Override
    public void onMouseDrag(MouseButton button, GameBoardManager manager, int gridX, int gridY) {

    }

    @Override
    public void onMouseRelease(MouseButton button, GameBoardManager manager, int cursorX, int cursorY) {

    }

    public StoneColor getStoneColor() {
        return stoneColor;
    }

    public void setStoneColor(StoneColor stoneColor) {
        this.stoneColor = stoneColor;
    }
}
