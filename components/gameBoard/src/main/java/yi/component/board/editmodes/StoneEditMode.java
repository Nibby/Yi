package yi.component.board.editmodes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import yi.component.board.GameBoardManager;
import yi.component.board.edits.StoneEdit;
import yi.core.go.GameNode;
import yi.core.go.GameNodeType;
import yi.core.go.Stone;
import yi.core.go.StoneColor;

public final class StoneEditMode extends AbstractEditMode {

    private final StoneColor colorToEdit;

    StoneEditMode(StoneColor colorToEdit) {
        if (colorToEdit == StoneColor.NONE) {
            throw new IllegalArgumentException("Stone color must not be NONE.");
        }

        this.colorToEdit = colorToEdit;
    }

    @Override
    public void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY) {
        
    }

    @Override
    public void onMousePress(MouseButton button, GameBoardManager manager, int gridX, int gridY) {
        editIntersectionAt(gridX, gridY, manager);
    }

    @Override
    public void onMouseDrag(MouseButton button, GameBoardManager manager, int gridX, int gridY) {
        // Not supported. Stone placement should be a careful act.
    }

    @Override
    public void onMouseRelease(MouseButton button, GameBoardManager manager, int cursorX, int cursorY) {

    }

    private void editIntersectionAt(int x, int y, GameBoardManager manager) {
        var stoneHere = manager.getGameModel().getCurrentGameState().getBoardPosition().getStoneColorAt(x, y);

        boolean createRatherThanDelete = stoneHere != colorToEdit;
        StoneColor thisActionColor = createRatherThanDelete ? colorToEdit : StoneColor.NONE;
        var currentNode = manager.getGameModel().getCurrentNode();
        GameNode nodeToEdit;

        if (currentNode.getType() != GameNodeType.STONE_EDIT) {
            nodeToEdit = null;
        } else {
            nodeToEdit = currentNode;
        }

        StoneEdit stoneEdit = new StoneEdit(nodeToEdit, new Stone(x, y, thisActionColor));
        manager.edit.recordAndApply(stoneEdit, manager);
    }
}
