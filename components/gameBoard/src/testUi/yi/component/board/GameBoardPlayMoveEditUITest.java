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
import yi.core.go.StandardGameRules;

import java.util.ArrayList;

@ExtendWith(ApplicationExtension.class)
public final class GameBoardPlayMoveEditUITest extends GameBoardUITestBase {

    @Start
    @Override
    public void start(Stage stage) {
        super.start(stage);

        getGameModel().onCurrentNodeChange().addListener((event) -> currentNodeChangeEventReceived = true);
        getGameModel().onCurrentNodeDataUpdate().addListener((event) -> Assertions.fail("Unexpected event received: onCurrentNodeDataUpdate()"));
        getGameModel().onNodeAdd().addListener((event) -> nodeAddEventReceived = true);
        getGameModel().onNodeRemove().addListener((event) -> Assertions.fail("Unexpected event received: onNodeRemove()"));
        getGameModel().onNodeDataUpdate().addListener((event) -> Assertions.fail("Unexpected event received: onNodeDataUpdate()"));
    }

    @AfterEach
    @Override
    protected void dispose() {
        super.dispose();
    }

    private boolean nodeAddEventReceived = false;
    private boolean currentNodeChangeEventReceived = false;

    @AfterEach
    public void afterTest() {
        nodeAddEventReceived = false;
        currentNodeChangeEventReceived = false;
    }

    @Test
    public void testPlayMove_ConsecutiveClicks_StateCorrect(FxRobot robot) {
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

        for (int i = 0; i < moves.size(); ++i) {
            click(robot, moves.get(i)[0], moves.get(i)[1]);
            assertStateCorrect(1 + i);
        }
    }

    private void assertStateCorrect(int currentMoveNumber) {
        var currentMove = getGameModel().getCurrentNode();

        Assertions.assertEquals(currentMoveNumber, currentMove.getMoveNumber(), "Current move number mismatch");
        Assertions.assertTrue(nodeAddEventReceived, "onNodeAdd() event was not received for move #" + currentMoveNumber);
        Assertions.assertTrue(currentNodeChangeEventReceived, "onCurrentNodeChange() event was not received for move #" + currentMoveNumber);

        var moveHistory = currentMove.getMoveHistory();
        Assertions.assertEquals(currentMoveNumber + 1, moveHistory.size(), "Move history size mismatch.");
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
