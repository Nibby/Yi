package yi.component.board.editmodes;

import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yi.component.board.GameBoardClassFactory;
import yi.component.board.GameBoardManagerAccessor;
import yi.models.go.GameModel;
import yi.models.go.StandardGameRules;
import yi.models.go.StoneColor;

public final class StoneEditModeUndoRedoTest {

    @Test
    public void testAddRemoveStone_UndoRedoWorks() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editStoneMode = EditMode.editStones(StoneColor.WHITE);
        manager.edit.setEditMode(editStoneMode);

        // Setup
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 0, 0);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 0, 1);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 0, 2);

        // Begin undo
        manager.edit.performUndo(manager);

        var position = model.getCurrentGameState().getBoardPosition();
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 2));
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 1));
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 0));

        manager.edit.performUndo(manager);
        position = model.getCurrentGameState().getBoardPosition();
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 2));
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 1));
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 0));

        manager.edit.performUndo(manager);
        position = model.getCurrentGameState().getBoardPosition();
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 2));
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 1));
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 0));

        // Begin redo
        manager.edit.performRedo(manager);
        position = model.getCurrentGameState().getBoardPosition();
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 2));
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 1));
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 0));

        manager.edit.performRedo(manager);
        position = model.getCurrentGameState().getBoardPosition();
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 2));
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 1));
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 0));

        manager.edit.performRedo(manager);
        position = model.getCurrentGameState().getBoardPosition();
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 0));
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 1));
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 0));
    }

    @Test
    public void testSubmitEditNode_UndoRedoWorks() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        // Setup
        var playMoveMode = EditMode.playMove();
        playMoveMode.onMousePress(MouseButton.PRIMARY, manager, 0, 0);

        var editStoneMode = EditMode.editStones(StoneColor.WHITE);
        manager.edit.setEditMode(editStoneMode);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 0, 0);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 0, 1);

        var position = model.getCurrentGameState().getBoardPosition();
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 0));
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 1));
        Assertions.assertNotEquals(model.getRootNode(), model.getCurrentNode());
        Assertions.assertEquals(2, model.getCurrentMoveNumber());

        // Begin undo
        manager.edit.performUndo(manager);
        position = model.getCurrentGameState().getBoardPosition();
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 0));
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 1));
        Assertions.assertNotEquals(model.getRootNode(), model.getCurrentNode());
        Assertions.assertEquals(2, model.getCurrentMoveNumber());

        // This undo should also remove the stone edit node, restoring current node back to move 1 -- the played black stone
        manager.edit.performUndo(manager);
        position = model.getCurrentGameState().getBoardPosition();
        Assertions.assertEquals(StoneColor.BLACK, position.getStoneColorAt(0, 0));
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 1));
        Assertions.assertEquals(model.getRootNode().getNextNodeInMainBranch(), model.getCurrentNode());
        Assertions.assertEquals(1, model.getCurrentMoveNumber());

        // Begin redo
        manager.edit.performRedo(manager);
        position = model.getCurrentGameState().getBoardPosition();
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 0));
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 1));
        Assertions.assertNotEquals(model.getRootNode(), model.getCurrentNode());
        Assertions.assertEquals(2, model.getCurrentMoveNumber());

        manager.edit.performRedo(manager);
        position = model.getCurrentGameState().getBoardPosition();
        Assertions.assertEquals(StoneColor.NONE, position.getStoneColorAt(0, 0));
        Assertions.assertEquals(StoneColor.WHITE, position.getStoneColorAt(0, 1));
        Assertions.assertNotEquals(model.getRootNode(), model.getCurrentNode());
        Assertions.assertEquals(2, model.getCurrentMoveNumber());
    }

}
