package yi.component.board;

import org.junit.jupiter.api.Assertions;
import yi.component.utilities.ComparisonUtilities;
import yi.component.utilities.ShapeUtilities;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.Test;

public final class GameBoardViewerMeasurementsTest {

    // TODO: These tests have been temporarily disabled because of frequent game board layout changes.
    //       Add them back once the board design has been finalized.
//    @Test
//    public void testBoardBoundsCorrect() {
//        GameBoardSize size = new GameBoardSize();
//        ThreadLocalRandom random = ThreadLocalRandom.current();
//
//        // Generate 100 random game board canvas dimensions
//        // and check the board region is calculated correctly.
//        // This should always be a square consisting of the minimum
//        // of width and height.
//        for (int i = 0; i < 100; ++i) {
//            double randomWidth = random.nextDouble(2000);
//            double randomHeight = random.nextDouble(2000);
//            double expectedBoardSize = Math.min(randomWidth, randomHeight);
//
//            size.recalculate(randomWidth, randomHeight, 0d);
//
//            Rectangle boardBounds = size.getBoardBounds();
//            String failMessage = "testSize=" + randomWidth + "x" + randomHeight + ", expected=" + expectedBoardSize + "x" + expectedBoardSize
//                    + ", got=" + boardBounds.getWidth() + "x" + boardBounds.getHeight();
//
//            Assertions.assertTrue(ComparisonUtilities.doubleEquals(expectedBoardSize, boardBounds.getWidth()), failMessage);
//            Assertions.assertTrue(ComparisonUtilities.doubleEquals(expectedBoardSize, boardBounds.getHeight()), failMessage);
//        }
//    }
//
//    @Test
//    public void testMarginPercentageClipCorrect() {
//        GameBoardSize size = new GameBoardSize();
//        final double componentSize = 100d;
//
//        // Test 1% -> 25% margin on a 100x100 board and ensure the bounds are calculated correctly
//        for (int iteration = 1; iteration < 25; iteration++) {
//            size.recalculate(componentSize, componentSize, iteration / 100d);
//            Rectangle boardBounds = size.getBoardBounds();
//
//            // 1% of component size
//            double percentage = componentSize / 100d;
//
//            double expectedXY = percentage*iteration;
//            double expectedWH = componentSize-(2*percentage*iteration);
//            String expectedBounds = "(" + expectedXY + "," + expectedXY + "," + expectedWH + "," + expectedWH + ") with tolerated imprecision +/- " + ComparisonUtilities.EPSILON;
//            String actualBounds = String.format("(%f, %f, %f, %f)", boardBounds.getX(), boardBounds.getY(), boardBounds.getWidth(), boardBounds.getHeight());
//            String failMessage = "margin=" + iteration +"% componentSize=" + componentSize + "x" + componentSize + ", expected=" + expectedBounds + ", got=" + actualBounds + ".";
//
//            Assertions.assertTrue(ComparisonUtilities.doubleEquals(expectedXY, boardBounds.getX()), failMessage + "Bad X value");
//            Assertions.assertTrue(ComparisonUtilities.doubleEquals(expectedXY, boardBounds.getY()), failMessage + "Bad Y value");
//            Assertions.assertTrue(ComparisonUtilities.doubleEquals(expectedWH, boardBounds.getWidth()), failMessage + "Bad WIDTH value");
//            Assertions.assertTrue(ComparisonUtilities.doubleEquals(expectedWH, boardBounds.getHeight()), failMessage + "Bad HEIGHT value");
//        }
//    }
}
