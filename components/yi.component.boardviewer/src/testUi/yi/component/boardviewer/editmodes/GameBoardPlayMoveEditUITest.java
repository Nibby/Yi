package yi.component.boardviewer.editmodes;

import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import yi.component.boardviewer.GameBoardUITestBase;
import yi.core.go.GameModel;
import yi.core.go.StandardGameRules;

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

            var undoSystem = getGameModel().getEditor().getUndoSystem();
            int undoSystemHistorySize = undoSystem.getEditHistorySize();
            Assertions.assertEquals(currentMoveNumber, undoSystemHistorySize,
                    "Undo system history size mismatch.");

            var moveHistory = currentMove.getMoveHistory();
            Assertions.assertEquals(currentMoveNumber + 1, moveHistory.size(),
                    "Move history size mismatch.");

            currentNodeChangeEventReceived.set(false);
            nodeAddEventReceived.set(false);
        }
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
