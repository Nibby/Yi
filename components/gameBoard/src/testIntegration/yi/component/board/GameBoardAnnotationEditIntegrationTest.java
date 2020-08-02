package yi.component.board;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;
import yi.component.board.editmodes.EditMode;
import yi.core.go.Annotation;
import yi.core.go.AnnotationType;
import yi.core.go.GameModel;
import yi.core.go.GameRules;

@ExtendWith(ApplicationExtension.class)
public final class GameBoardAnnotationEditIntegrationTest {

    private static final int BOARD_WIDTH = 9;
    private static final int BOARD_HEIGHT = 9;

    private GameBoardViewer board;
    private GameModel gameModel;

    @Start
    public void start(Stage primaryStage) {
        gameModel = new GameModel(BOARD_WIDTH, BOARD_HEIGHT, GameRules.CHINESE);
        board = new GameBoardViewer();
        board.setGameModel(gameModel);

        var container = new BorderPane();
        container.setCenter(board.getComponent());

        var scene = new Scene(container, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.requestFocus();
    }

    @Test
    public void testAddOne_PointAnnotation(FxRobot robot) {
        board.setEditMode(EditMode.annotation(AnnotationType.SQUARE));

        var size = board._getManager().size;
        double[] intersection = size.getGridRenderPosition(0, 0, 0);
        double x = intersection[0];
        double y = intersection[1];

        robot.moveTo(board.getComponent(), Pos.TOP_LEFT, new Point2D(x, y), Motion.DEFAULT);
        robot.clickOn(MouseButton.PRIMARY);

        Assertions.assertTrue(gameModel.getCurrentMoveStateDelta().getAnnotationsOnThisNode().contains(new Annotation.Square(0, 0)),
                "No square annotation created after mouse click.");
    }

    @Test
    public void testAddMultiple_PointAnnotation(FxRobot robot) {
        board.setEditMode(EditMode.annotation(AnnotationType.SQUARE));

        var size = board._getManager().size;
        double[] startPoint = size.getGridRenderPosition(0, 0, 0);
        double xStart = startPoint[0];
        double yStart = startPoint[1];

        double[] endPoint = size.getGridRenderPosition(BOARD_WIDTH - 1, 0, 0);
        double xEnd = endPoint[0];
        double yEnd = endPoint[1];

        robot.moveTo(board.getComponent(), Pos.TOP_LEFT, new Point2D(xStart, yStart), Motion.DEFAULT);
        robot.drag(MouseButton.PRIMARY);
        robot.moveTo(board.getComponent(), Pos.TOP_LEFT, new Point2D(xEnd, yEnd), Motion.DEFAULT);
        robot.release(MouseButton.PRIMARY);

        for (int i = 0; i < BOARD_WIDTH; ++i) {
            Assertions.assertTrue(gameModel.getCurrentMoveStateDelta().getAnnotationsOnThisNode().contains(new Annotation.Square(i, 0)),
                    "No square annotation created at (" + i + ", 0) after mouse drag from (0, 0) to (" + (BOARD_WIDTH-1) + ", 0).");
        }
    }

    @Test
    public void testEraseMultiple_PointAnnotation(FxRobot robot) {
        board.setEditMode(EditMode.annotation(AnnotationType.SQUARE));

        var size = board._getManager().size;
        double[] startPoint = size.getGridRenderPosition(0, 0, 0);
        double xStart = startPoint[0];
        double yStart = startPoint[1];

        double[] endPoint = size.getGridRenderPosition(BOARD_WIDTH - 1, 0, 0);
        double xEnd = endPoint[0];
        double yEnd = endPoint[1];

        robot.moveTo(board.getComponent(), Pos.TOP_LEFT, new Point2D(xStart, yStart), Motion.DEFAULT);
        robot.drag(MouseButton.PRIMARY);
        robot.moveTo(board.getComponent(), Pos.TOP_LEFT, new Point2D(xEnd, yEnd), Motion.DEFAULT);
        robot.release(MouseButton.PRIMARY);
        robot.sleep(100);
        robot.drag(MouseButton.PRIMARY);
        robot.moveTo(board.getComponent(), Pos.TOP_LEFT, new Point2D(xStart, yStart), Motion.DEFAULT);
        robot.release(MouseButton.PRIMARY);

        Assertions.assertEquals(0, gameModel.getCurrentMoveStateDelta().getAnnotationsOnThisNode().size(),
                "Annotations are not fully cleared.");
    }

    @Test
    public void testAddMultiple_PointAnnotation_ReplaceExistingAnnotations(FxRobot robot) {
        board.setEditMode(EditMode.annotation(AnnotationType.SQUARE));

        var size = board._getManager().size;
        double[] startPoint = size.getGridRenderPosition(0, 0, 0);
        double xStart = startPoint[0];
        double yStart = startPoint[1];

        double[] endPoint = size.getGridRenderPosition(BOARD_WIDTH - 1, 0, 0);
        double xEnd = endPoint[0];
        double yEnd = endPoint[1];

        robot.moveTo(board.getComponent(), Pos.TOP_LEFT, new Point2D(xStart, yStart), Motion.DEFAULT);
        robot.drag(MouseButton.PRIMARY);
        robot.moveTo(board.getComponent(), Pos.TOP_LEFT, new Point2D(xEnd, yEnd), Motion.DEFAULT);
        robot.release(MouseButton.PRIMARY);

        board.setEditMode(EditMode.annotation(AnnotationType.CROSS));

        robot.drag(MouseButton.PRIMARY);
        robot.moveTo(board.getComponent(), Pos.TOP_LEFT, new Point2D(xStart, yStart), Motion.DEFAULT);
        robot.release(MouseButton.PRIMARY);

        for (int i = 0; i < BOARD_WIDTH; ++i) {
            Assertions.assertTrue(gameModel.getCurrentMoveStateDelta().getAnnotationsOnThisNode().contains(new Annotation.Cross(i, 0)),
                    "No cross annotation at (" + i + ", 0) after mouse drag from (0, 0) to (" + (BOARD_WIDTH-1) + ", 0) to replace the row of squares.");
        }
    }


}
