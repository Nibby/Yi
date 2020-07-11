package yi.models.yi.editor.gui.board;

import yi.models.yi.editor.utilities.ComparisonUtilities;
import yi.models.yi.editor.utilities.ShapeUtilities;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class GameBoardMeasurementsTest {

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

    @Test
    public void testCenterFit_squareContainerNoInsets_squareFitRatio() {
        Rectangle container = new Rectangle(0, 0, 100, 100);
        double fitWidthToHeightRatio = 1.0d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(0, 0, 100, 100), fit);
    }

    @Test
    public void testCenterFit_squareContainerNoInsets_wideFitRatio() {
        Rectangle container = new Rectangle(0, 0, 100, 100);
        double fitWidthToHeightRatio = 2.0d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(0, 25, 100, 50), fit);
    }

    @Test
    public void testCenterFit_squareContainerNoInsets_tallFitRatio() {
        Rectangle container = new Rectangle(0, 0, 100, 100);
        double fitWidthToHeightRatio = 0.5d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(25, 0, 50, 100), fit);
    }

    @Test
    public void testCenterFit_wideContainerNoInsets_squareFitRatio() {
        Rectangle container = new Rectangle(0, 0, 200, 100);
        double fitWidthToHeightRatio = 1.0d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(50, 0, 100, 100), fit);
    }

    @Test
    public void testCenterFit_wideContainerNoInsets_wideFitRatio() {
        Rectangle container = new Rectangle(0, 0, 200, 100);
        double fitWidthToHeightRatio = 2.0d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(0, 0, 200, 100), fit);
    }

    @Test
    public void testCenterFit_wideContainerNoInsets_tallFitRatio() {
        Rectangle container = new Rectangle(0, 0, 200, 100);
        double fitWidthToHeightRatio = 0.5d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(75, 0, 50, 100), fit);
    }

    @Test
    public void testCenterFit_tallContainerNoInsets_squareFitRatio() {
        Rectangle container = new Rectangle(0, 0, 100, 200);
        double fitWidthToHeightRatio = 1.0d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(0, 50, 100, 100), fit);
    }

    @Test
    public void testCenterFit_tallContainerNoInsets_wideFitRatio() {
        Rectangle container = new Rectangle(0, 0, 100, 200);
        double fitWidthToHeightRatio = 2.0d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(0, 75, 100, 50), fit);
    }

    @Test
    public void testCenterFit_tallContainerNoInsets_tallFitRatio() {
        Rectangle container = new Rectangle(0, 0, 100, 200);
        double fitWidthToHeightRatio = 0.5d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(0, 0, 100, 200), fit);
    }

    @Test
    public void testCenterFit_tallContainerFivePercentInsets_tallFitCenteredCorrectly() {
        Rectangle container = new Rectangle(0, 0, 100, 200);
        double fitWidthToHeightRatio = 0.5d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0.05d);

        testFitCorrect(new Rectangle(5, 5, 90, 190), fit);
    }

    private void testFitCorrect(Rectangle expected, Rectangle actual) {
        System.out.println("Testing fitExpected=" + expected.toString() + ", actual=" + actual.toString());

        assertTrue(ComparisonUtilities.doubleEquals(actual.getX(), expected.getX()));
        assertTrue(ComparisonUtilities.doubleEquals(actual.getY(), expected.getY()));
        assertTrue(ComparisonUtilities.doubleEquals(actual.getWidth(), expected.getWidth()));
        assertTrue(ComparisonUtilities.doubleEquals(actual.getHeight(), expected.getHeight()));
    }
}
