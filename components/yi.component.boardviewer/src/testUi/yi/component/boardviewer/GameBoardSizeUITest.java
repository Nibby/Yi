package yi.component.boardviewer;

import javafx.application.Platform;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import yi.core.go.GameModel;
import yi.core.go.StandardGameRules;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A general smoke test of board rendering sizes. Will save screenshots to the module output
 * directory if a test case fails.
 *
 * It is recommended that these tests are run through gradle via {@code ./gradlew :components:gameBoard:testUi}
 * as the script will properly post-process the test output data.
 */
@ExtendWith(ApplicationExtension.class)
public final class GameBoardSizeUITest extends GameBoardUITestBase {

    @Start
    public void start(Stage stage) {
        super.start(stage);
    }

    @Override
    protected void dispose() {
        super.dispose();
    }

    @Test
    public void testSupportedSizes() {
        int supportedSizes = 24; //  1x1 -> 25x25
        getBoardManager().setDebugMode(true);

        for (int w = 1; w < supportedSizes; ++w) {
            for (int h = 1; h < supportedSizes; ++h) {
                int finalW = w;
                int finalH = h;
                Platform.runLater(() -> {
                    synchronized (GameBoardSizeUITest.class) {
                        var newModel = new GameModel(finalW, finalH, StandardGameRules.CHINESE);
                        setGameModel(newModel);
                        assertSizeCorrect();
                    }
                });
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void assertSizeCorrect() {
        var boardWidth = getBoardManager().getGameModel().getBoardWidth();
        var boardHeight = getBoardManager().getGameModel().getBoardHeight();

        var sizeManager = getBoardManager().size;
        var stoneSize = sizeManager.getStoneSizeInPixels();

        Map<String, Rectangle> bounds = new HashMap<>();
        bounds.put("stage", sizeManager.getStageBounds());
        bounds.put("board", sizeManager.getBoardBounds());

        for (String boundName : bounds.keySet()) {
            Rectangle bound = bounds.get(boundName);

            for (int i = 0; i < boardWidth; ++i) {
                var stoneBounds = sizeManager.getStoneRenderPosition(i, 0);
                var y = stoneBounds[1];
                if (y < bound.getY()) {
                    failIt(i, 0, boardWidth, boardHeight, boundName);
                }

                var bottomRow = boardHeight-1;
                stoneBounds = sizeManager.getStoneRenderPosition(i, bottomRow);
                y = stoneBounds[1];
                if ((y + stoneSize) > bound.getY() + bound.getHeight()) {
                    failIt(i, bottomRow, boardWidth, boardHeight, boundName);
                }
            }

            for (int i = 0; i < boardHeight; ++i) {
                var stoneBounds = sizeManager.getStoneRenderPosition(0, i);
                var x = stoneBounds[0];
                if (x < bound.getX()) {
                    failIt(0, i, boardWidth, boardHeight, boundName);
                }

                var rightmostCol = boardWidth-1;
                stoneBounds = sizeManager.getStoneRenderPosition(rightmostCol, i);
                x = stoneBounds[0];
                if ((x + stoneSize) > bound.getX() + bound.getWidth()) {
                    failIt(rightmostCol, i, boardWidth, boardHeight, boundName);
                }
            }
        }
    }

    private void failIt(int x, int y, int boardWidth, int boardHeight, String boundName) {
        String failMessage = "On " + boardWidth + "x" + boardHeight + " board, " +
                "move at (" + x + ", " + y + ") is rendered out of " + boundName + " bounds.";

        try {
            getGameModel().getEditor().addMoveWithoutValidation(x, y);
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
    protected StandardGameRules getGameRules() {
        return StandardGameRules.CHINESE;
    }
}
