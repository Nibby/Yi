package yi.component.gametree;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yi.component.utilities.ComparisonUtilities;

import java.util.concurrent.atomic.AtomicBoolean;

public class GameTreeViewerCameraTest {

    // TODO: Doesn't work, probably because events must be invoked on a Fx thread. Use a Fx UI testing framework to test this.
//    @Test
//    public void testAnimatedPan_FiresMultipleIntermediateEvents_HasCorrectPath() throws InterruptedException {
//        double viewportWidth = 100;
//        double viewportHeight = 100;
//
//        // Setup
//        var camera = new Camera(viewportWidth, viewportHeight);
//        camera.setOffset(0, 0);
//
//        var eventsReceived = new AtomicInteger(0);
//        var lastOffsetX = new AtomicInteger(0);
//        var lastOffsetY = new AtomicInteger(0);
//
//        camera.addOffsetChangeListener(() -> {
//            eventsReceived.incrementAndGet();
//
//            double offsetX = camera.getOffsetX();
//            double offsetY = camera.getOffsetY();
//
//            Assertions.assertTrue(offsetX > lastOffsetX.get());
//            Assertions.assertTrue(offsetY > lastOffsetY.get());
//
//            lastOffsetX.set((int) Math.round(offsetX));
//            lastOffsetY.set((int) Math.round(offsetY));
//        });
//
//        // Test
//        var running = new AtomicBoolean(true);
//        new Thread(() -> {
//            camera.setCenterOnCoordinateWithAnimation(100, 100);
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            running.set(false);
//        }).start();
//
//        while (running.get()) {
//            Thread.sleep(250);
//        }
//
//        // Assert
//        Assertions.assertTrue(eventsReceived.get() >= 5, "Received only " + eventsReceived.get() + " events out of 5");
//        Assertions.assertEquals(100, lastOffsetX.get());
//        Assertions.assertEquals(100, lastOffsetY.get());
//    }

    @Test
    public void testViewportCentering_SetCenterImmediately_Works() {
        // Arrange
        double viewportWidth = 100;
        double viewportHeight = 100;

        var camera = new Camera(0, 0, viewportWidth, viewportHeight);

        // Act
        camera.setCenterOnCoordinateImmediately(50, 50);

        // Assert
        Assertions.assertTrue(ComparisonUtilities.doubleEquals(0d, camera.getOffsetX()));
        Assertions.assertTrue(ComparisonUtilities.doubleEquals(0d, camera.getOffsetY()));
    }

    @Test
    public void testViewportAdjustment_CenterPointDoesNotChange() {
        // Arrange
        double viewportWidth = 100;
        double viewportHeight = 100;

        var camera = new Camera(viewportWidth, viewportHeight);
        camera.setCenterOnCoordinateImmediately(50, 50); // OffsetX, OffsetY should be (0, 0)

        // Act
        camera.setViewportSize(200, 200);

        // Assert
        Assertions.assertTrue(ComparisonUtilities.doubleEquals(camera.getCenterX(), 50));
        Assertions.assertTrue(ComparisonUtilities.doubleEquals(camera.getCenterY(), 50));

        Assertions.assertTrue(ComparisonUtilities.doubleEquals(camera.getOffsetX(), 50));
        Assertions.assertTrue(ComparisonUtilities.doubleEquals(camera.getOffsetY(), 50));
    }

    @Test
    public void testViewportAdjustment_FiresExactlyOneEvent() throws InterruptedException {
        // Arrange
        double viewportWidth = 100;
        double viewportHeight = 100;

        var camera = new Camera(viewportWidth, viewportHeight);
        camera.setCenterOnCoordinateImmediately(50, 50); // OffsetX, OffsetY should be (0, 0)

        var eventReceived = new AtomicBoolean(false);

        camera.addOffsetChangeListener(() -> {
            if (eventReceived.get()) {
                Assertions.fail("More than one event received!");
            }

            eventReceived.set(true);
        });

        // Act
        camera.setViewportSize(200, 200);

        // Assert
        Thread.sleep(50);

        Assertions.assertTrue(eventReceived.get());
    }

}
