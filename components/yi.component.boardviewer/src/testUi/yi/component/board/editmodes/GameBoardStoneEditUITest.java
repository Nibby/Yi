package yi.component.board.editmodes;

import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;
import yi.component.board.GameBoardUITestBase;
import yi.models.go.StandardGameRules;
import yi.models.go.StoneColor;

@ExtendWith(ApplicationExtension.class)
public final class GameBoardStoneEditUITest extends GameBoardUITestBase {

    @Start
    @Override
    public void start(Stage stage) {
        super.start(stage);
    }

    @Test
    public void testClickToAdd_clickAgainToRemove(FxRobot robot) {
        getBoard().setEditMode(EditMode.editStones(StoneColor.WHITE));

        robot.moveTo(getBoard().getComponent(), Pos.TOP_LEFT, getIntersection(0, 0), Motion.DEFAULT);
        robot.clickOn(MouseButton.PRIMARY);

        Assertions.assertEquals(StoneColor.WHITE, getStoneAt(0, 0), "Click once did not place stone");

        robot.clickOn(MouseButton.PRIMARY);

        Assertions.assertEquals(StoneColor.NONE, getStoneAt(0, 0), "Click on existing edit stone did not remove it");
    }

    @Test
    public void testDragAcross_onlyCreateStoneAtClickPoint(FxRobot robot) {
        getBoard().setEditMode(EditMode.editStones(StoneColor.BLACK));
        drag(robot, 0, 0, 8, 0);
        Assertions.assertEquals(StoneColor.BLACK, getStoneAt(0, 0), "Mouse drag did not create stone at clicked intersection");
        for (int i = 1; i < 9; ++i) {
            Assertions.assertEquals(StoneColor.NONE, getStoneAt(i, 0), "Mouse drag created stone along drag path");
        }
    }

    @Test
    public void testClickOnExistingEditStone_withDifferentEditColor_removesItAnyway(FxRobot robot) {
        getBoard().setEditMode(EditMode.editStones(StoneColor.BLACK));
        click(robot, 0, 0);
        getBoard().setEditMode(EditMode.editStones(StoneColor.WHITE));
        click(robot, 0, 0);

        Assertions.assertEquals(StoneColor.NONE, getStoneAt(0, 0), "Click on existing edit stone using different stone color did not remove original stone");
    }

    @Test
    public void testClickOnExistingStoneFromPlayMoveNode_removesIt(FxRobot robot) {
        getBoard().setEditMode(EditMode.playMove());
        click(robot, 0, 0);
        getBoard().setEditMode(EditMode.editStones(StoneColor.WHITE));
        click(robot, 0, 0);

        Assertions.assertEquals(StoneColor.NONE, getStoneAt(0, 0), "Click on existing played move did not remove the stone");
    }

    @Override
    protected int getBoardWidth() {
        return 9;
    }

    @Override
    protected int getBoardHeight() {
        return 9;
    }

    @Override
    protected StandardGameRules getGameRules() {
        return StandardGameRules.CHINESE;
    }
}
