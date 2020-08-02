package yi.component.board;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import yi.core.go.GameModel;
import yi.core.go.GameRules;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A general smoke test of board rendering sizes. Will save screenshots to the module output
 * directory if a test case fails.
 *
 * It is recommended that these tests are run through gradle via {@code ./gradlew :components:gameBoard:testUi}
 * as the script will properly post-process the test output data.
 */
@ExtendWith(ApplicationExtension.class)
public class GameBoardSizeUITest extends GameBoardUITestBase {

    @Start
    public void start(Stage stage) {
        super.start(stage);
    }

    @Override
    protected void dispose() {
        super.dispose();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    public void testSupportedSizes(FxRobot robot) throws InterruptedException {
        int supportedSizes = 24; //  1x1 -> 25x25

        for (int w = 1; w < supportedSizes; ++w) {
            for (int h = 1; h < supportedSizes; ++h) {
                int finalW = w;
                int finalH = h;
                Platform.runLater(() -> {
                    synchronized (GameBoardSizeUITest.class) {
                        var newModel = new GameModel(finalW, finalH, GameRules.CHINESE);
                        setGameModel(newModel);
                        assertSizeCorrect();
                    }
                });
                Thread.sleep(10);
            }
        }
    }

    private void assertSizeCorrect() {
        var boardWidth = getBoardManager().model.getBoardWidth();
        var boardHeight = getBoardManager().model.getBoardHeight();

        var sizeManager = getBoardManager().size;
        var stoneSize = sizeManager.getStoneSizeInPixels();
        var stage = sizeManager.getStageBounds();

        for (int i = 0; i < boardWidth; ++i) {
            var stoneBounds = sizeManager.getStoneRenderPosition(i, 0);
            var y = stoneBounds[1];
            if (y < stage.getY()) {
                failIt(i, 0, boardWidth, boardHeight);
            }

            var bottomRow = boardHeight-1;
            stoneBounds = sizeManager.getStoneRenderPosition(i, bottomRow);
            y = stoneBounds[1];
            if ((y + stoneSize) > stage.getHeight()) {
                failIt(i, bottomRow, boardWidth, boardHeight);
            }
        }

        for (int i = 0; i < boardHeight; ++i) {
            var stoneBounds = sizeManager.getStoneRenderPosition(0, i);
            var x = stoneBounds[0];
            if (x < stage.getX()) {
                failIt(0, i, boardWidth, boardHeight);
            }

            var rightmostCol = boardWidth-1;
            stoneBounds = sizeManager.getStoneRenderPosition(rightmostCol, i);
            x = stoneBounds[0];
            if ((x + stoneSize) > stage.getWidth()) {
                failIt(rightmostCol, i, boardWidth, boardHeight);
            }
        }

    }

    private void failIt(int x, int y, int boardWidth, int boardHeight) {
        String failMessage = "On " + boardWidth + "x" + boardHeight + " board, " +
                "move at (" + x + ", " + y + ") is rendered out of bounds.";

        try {
            getGameModel().playMoveIgnoringRules(x, y);
            String screenshotName = boardWidth +"x" + boardHeight + "_at_" + x + "_" + y + ".png";
            boolean success = saveScreenshot("GameBoardSizeUITest", screenshotName);

            if (success) {
                failMessage += "\n A screenshot has been saved at \"" + screenshotName + "\"";
            } else {
                failMessage += "\n Failed to save screenshot.";
            }
        } catch (IOException e) {
            failMessage += "\n Failed to save screenshot.";
        }

        Assertions.fail(failMessage);
    }

    @Override
    protected int getBoardWidth() {
        return 25;
    }

    @Override
    protected int getBoardHeight() {
        return 25;
    }

    @NotNull
    @Override
    protected GameRules getGameRules() {
        return GameRules.CHINESE;
    }
}
