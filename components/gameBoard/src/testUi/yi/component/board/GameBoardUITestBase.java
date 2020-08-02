package yi.component.board;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;
import yi.core.go.Annotation;
import yi.core.go.GameModel;
import yi.core.go.GameRules;

import static org.assertj.core.api.Assertions.*;

public abstract class GameBoardUITestBase {

    private GameBoardViewer board;
    private GameModel gameModel;
    private Stage stage;

    @Start
    public void start(Stage stage) {
        gameModel = new GameModel(getBoardWidth(), getBoardHeight(), getGameRules());
        board = new GameBoardViewer();
        board.setGameModel(gameModel);

        var container = new BorderPane();
        container.setCenter(board.getComponent());

        var scene = new Scene(container, 600, 600);
        stage.setScene(scene);
        stage.show();
        stage.requestFocus();

        this.stage = stage;
    }

    @AfterEach
    protected void dispose() {
        Platform.runLater(() -> this.stage.close());
        Runtime.getRuntime().gc();

        this.board = null;
        this.gameModel.dispose();
        this.gameModel = null;
    }

    protected abstract int getBoardWidth();
    protected abstract int getBoardHeight();
    protected abstract GameRules getGameRules();

    protected void drag(FxRobot robot, int xStart, int yStart, int xEnd, int yEnd) {
        robot.moveTo(board.getComponent(), Pos.TOP_LEFT, getIntersection(xStart, yStart), Motion.DEFAULT);
        robot.drag(MouseButton.PRIMARY);
        robot.moveTo(board.getComponent(), Pos.TOP_LEFT, getIntersection(xEnd, yEnd), Motion.DEFAULT);
        robot.release(MouseButton.PRIMARY);
    }

    protected void click(FxRobot robot, int x, int y) {
        robot.moveTo(board.getComponent(), Pos.TOP_LEFT, getIntersection(x, y), Motion.DEFAULT);
        robot.clickOn(MouseButton.PRIMARY);
    }

    protected Point2D getIntersection(int x, int y) {
        var size = board._getManager().size;
        double[] intersectionOnScreen = size.getGridRenderPosition(x, y, 0);
        double xScreen = intersectionOnScreen[0];
        double yScreen = intersectionOnScreen[1];

        return new Point2D(xScreen, yScreen);
    }

    protected Annotation getAnnotationOrFail(int x, int y) {
        return getGameModel().getCurrentMoveStateDelta().getAnnotationsOnThisNode().stream()
                .filter(annotation -> annotation.isOccupyingPosition(x, y))
                .findAny()
                .orElseGet(() -> {
                    fail("Expected an annotation at (" + x + ", " + y + ") but got none.");
                    return null;
                });
    }

    public GameBoardViewer getBoard() {
        return board;
    }

    public GameBoardManager getBoardManager() {
        return board._getManager();
    }

    public GameModel getGameModel() {
        return gameModel;
    }
}
