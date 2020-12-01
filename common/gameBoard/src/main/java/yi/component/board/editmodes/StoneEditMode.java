package yi.component.board.editmodes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import yi.component.board.GameBoardManager;
import yi.component.board.edits.StoneEdit;
import yi.models.go.GameNode;
import yi.models.go.GameNodeType;
import yi.models.go.StoneColor;

final class StoneEditMode extends AbstractEditMode {

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
        var currentNode = manager.getGameModel().getCurrentNode();
        GameNode nodeToEdit;

        if (currentNode.getType() != GameNodeType.STONE_EDIT && currentNode.getType() != GameNodeType.ROOT) {
            nodeToEdit = null;
        } else {
            nodeToEdit = currentNode;
        }

        var stoneHere = manager.getGameModel().getCurrentGameState().getBoardPosition().getStoneColorAt(x, y);
        var stoneEditHere = currentNode.getStoneEditAt(x, y);
        boolean stoneExistsHere = stoneHere != StoneColor.NONE;
        StoneEdit edit;

        if (stoneExistsHere) {
            // There are two ways to effectively 'remove' a stone from the game position
            // 1. Create a StoneEdit which sets the stone color at (x, y) to NONE
            // 2. Remove the stone edit which created the stone in the first place.
            //
            // Method 2 is applied only if the stone edit appears on the same node as the node to be edited.
            // Otherwise use method 1 to erase stones created from another node.
            if (nodeToEdit == null || stoneEditHere == null) {
                // No edit on this node created this stone, apply method 1
                edit = StoneEdit.add(nodeToEdit, x, y, StoneColor.NONE);
            } else {
                // Another edit on this node created this stone, apply method 2
                edit = StoneEdit.remove(nodeToEdit, x, y);
            }
        } else if (stoneEditHere != null) {
            // There may not be a stone here, but that could either mean:
            // 1. There is no stone edit here either (the intersection has always been empty)
            // 2. There was a stone last move, and a stone edit of StoneColor.NONE exists here
            //    which erased it from the position.
            assert stoneEditHere.getColor() == StoneColor.NONE;
            edit = StoneEdit.remove(nodeToEdit, x, y);
        } else {
            // Assuming intersection here has always been empty.
            edit = StoneEdit.add(nodeToEdit, x, y, colorToEdit);
        }

        manager.edit.recordAndApply(edit, manager);
    }
}
