package yi.component.board;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;
import yi.core.go.Annotation;
import yi.core.go.GameModel;
import yi.core.go.StandardGameRules;
import yi.core.go.StoneColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.fail;

public abstract class GameBoardUITestBase {

    private GameBoardViewer board;
    private GameModel gameModel;
    protected Stage stage;

    @Start
    public void start(Stage stage) {
        this.stage = stage;
        board = new GameBoardViewer();

        setGameModel(new GameModel(getBoardWidth(), getBoardHeight(), getGameRules()));

        var container = new BorderPane();
        container.setCenter(board.getComponent());

        var scene = new Scene(container, 600, 600);
        stage.setScene(scene);

        this.stage.show();
        this.stage.requestFocus();
    }

    protected void setGameModel(GameModel gameModel) {
        this.gameModel = gameModel;
        board.setGameModel(gameModel);
    }

    @AfterEach
    protected void dispose() {
        Platform.runLater(() -> this.stage.close());
        Runtime.getRuntime().gc();

        this.board = null;
        this.gameModel.dispose();
        this.gameModel = null;

        System.out.println("Used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
        System.out.println("Total memory: " + Runtime.getRuntime().totalMemory() / 1024 / 1024);
        System.out.println("Max memory: " + Runtime.getRuntime().maxMemory() / 1024 / 1024);
        System.out.println();
    }

    protected abstract int getBoardWidth();
    protected abstract int getBoardHeight();
    protected abstract StandardGameRules getGameRules();

    protected boolean saveScreenshot(String folderName, String fileName) throws IOException {
        var image = new WritableImage((int) stage.getWidth(), (int) stage.getHeight());
        var snapshot = stage.getScene().snapshot(image);
        var snapshotBufferedImage = SwingFXUtils.fromFXImage(snapshot, new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB));

        var screenshotDir = Paths.get("testScreenshots").resolve(folderName);

        if (!Files.exists(screenshotDir)) {
            Files.createDirectories(screenshotDir);
        }

        var screenshotFile = screenshotDir.resolve(fileName);

        if (Files.exists(screenshotFile) && Files.isRegularFile(screenshotFile)) {
            Files.delete(screenshotFile);
        }
        return ImageIO.write(snapshotBufferedImage, "png", screenshotFile.toFile());
    }

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
        return getGameModel().getCurrentNode().getAnnotations().stream()
                .filter(annotation -> annotation.isOccupyingPosition(x, y))
                .findAny()
                .orElseGet(() -> {
                    fail("Expected an annotation at (" + x + ", " + y + ") but got none.");
                    return null;
                });
    }

    protected StoneColor getStoneAt(int x, int y) {
        return getGameModel().getCurrentGameState().getBoardPosition().getStoneColorAt(x, y);
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
