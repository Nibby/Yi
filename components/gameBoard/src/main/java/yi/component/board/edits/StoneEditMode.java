package yi.component.board.edits;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import yi.component.board.GameBoardManager;
import yi.core.go.StoneColor;

public final class StoneEditMode implements EditMode {

    private StoneColor stoneColor;

    public StoneEditMode(StoneColor stoneColor) {
        this.stoneColor = stoneColor;
    }
    
    @Override
    public void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY) {

    }

    @Override
    public void onMousePress(GameBoardManager manager, int gridX, int gridY) {

    }

    @Override
    public void onMouseDrag(GameBoardManager manager, int gridX, int gridY) {

    }

    @Override
    public void onKeyPress(GameBoardManager manager, KeyEvent e) {

    }

    public StoneColor getStoneColor() {
        return stoneColor;
    }

    public void setStoneColor(StoneColor stoneColor) {
        this.stoneColor = stoneColor;
    }
}
