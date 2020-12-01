package yi.component.board;

import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import yi.component.board.editmodes.EditMode;
import yi.component.board.edits.PlayMoveEdit;
import yi.models.go.GameModel;
import yi.models.go.StandardGameRules;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@ExtendWith(ApplicationExtension.class)
public final class GameBoardPlayMoveEditUITest extends GameBoardUITestBase {

    @Start
    @Override
    public void start(Stage stage) {
        super.start(stage);
    }

    @AfterEach
    @Override
    protected void dispose() {
        super.dispose();
    }

    @Test
    public void testSimpleEdits(FxRobot robot) {
        var currentNodeChangeEventReceived = new AtomicBoolean(false);
        var nodeAddEventReceived = new AtomicBoolean(false);

        setGameModel(new GameModel(19, 19, StandardGameRules.CHINESE));
        getGameModel().onCurrentNodeChange().addListener((event) -> currentNodeChangeEventReceived.set(true));
        getGameModel().onCurrentNodeDataUpdate().addListener((event) -> Assertions.fail("Unexpected event received: onCurrentNodeDataUpdate()"));
        getGameModel().onNodeAdd().addListener((event) -> nodeAddEventReceived.set(true));
        getGameModel().onNodeRemove().addListener((event) -> Assertions.fail("Unexpected event received: onNodeRemove()"));
        getGameModel().onNodeDataUpdate().addListener((event) -> Assertions.fail("Unexpected event received: onNodeDataUpdate()"));
        getBoard().setEditMode(EditMode.playMove());

        var moves = new ArrayList<int[]>();
        moves.add(new int[] { 3, 3 });
        moves.add(new int[] { 15, 3 });
        moves.add(new int[] { 3, 16 });
        moves.add(new int[] { 15, 16 });
        moves.add(new int[] { 15, 14 });
        moves.add(new int[] { 16, 14 });
        moves.add(new int[] { 16, 13 });
        moves.add(new int[] { 16, 15 });
        moves.add(new int[] { 15, 13 });
        moves.add(new int[] { 13, 16 });
        moves.add(new int[] { 16, 9 });

        for (int[] move : moves) {
            click(robot, move[0], move[1]);

            var currentMove = getGameModel().getCurrentNode();
            var currentMoveNumber = currentMove.getMoveNumber();

            Assertions.assertEquals(currentMoveNumber, currentMove.getMoveNumber(),
                    "Current move number mismatch");
            Assertions.assertTrue(nodeAddEventReceived.get(),
                    "onNodeAdd() event was not received for move #" + currentMoveNumber);
            Assertions.assertTrue(currentNodeChangeEventReceived.get(),
                    "onCurrentNodeChange() event was not received for move #" + currentMoveNumber);

            int undoSystemHistorySize = getBoard().getManager().edit.getCurrentHistorySize();
            Assertions.assertEquals(currentMoveNumber, undoSystemHistorySize,
                    "Undo system history size mismatch.");

            var moveHistory = currentMove.getMoveHistory();
            Assertions.assertEquals(currentMoveNumber + 1, moveHistory.size(),
                    "Move history size mismatch.");

            currentNodeChangeEventReceived.set(false);
            nodeAddEventReceived.set(false);
        }
    }

    @Test
    public void testEditAtSameLocationAsNextMoveDoesNotCreateEditInUndoSystem(FxRobot robot) {
        // Sorry about the name! This test checks for clicking on the same spot
        // as the next move causes the board to change state to that move and
        // no new PlayMoveEdit is created in the undo system. This avoids
        // corrupting undo history.
        var model = new GameModel(19, 19, StandardGameRules.CHINESE);
        model.beginMoveSequence().playMove(0, 0);
        model.setCurrentNode(model.getRootNode());
        setGameModel(model);

        click(robot, 0, 0); // We're at root, now click on the same location as the first move

        Assertions.assertEquals(model.getRootNode().getNextNodeInMainBranch(), model.getCurrentNode(),
                "Current node is not at the expected position after clicking on the " +
                        "same location as the next move in the model.");

        // No new edits should be created for clicking on a continuation.
        Assertions.assertEquals(0, getBoard().getManager().edit.getCurrentHistorySize(),
                "Undo system registered a new " + PlayMoveEdit.class.getSimpleName() + " " +
                        "when we clicked on the same location the next move in the " +
                        "game model.");
    }

    @Override
    protected int getBoardWidth() {
        return 19;
    }

    @Override
    protected int getBoardHeight() {
        return 19;
    }

    @Override
    protected StandardGameRules getGameRules() {
        return StandardGameRules.CHINESE;
    }
}
