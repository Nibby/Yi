package yi.component.gametree;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Dimension2D;
import javafx.util.Duration;
import yi.component.utilities.ComparisonUtilities;

import java.util.HashSet;
import java.util.Set;

/**
 * A drawing offset manager that determines the drawing position of all the elements on the tree canvas.
 * Supports a smooth panning animation.
 */
final class Camera {

    private double offsetX = 0d;
    private double offsetY = 0d;

    private double stepSizeX = 0d;
    private double stepSizeY = 0d;

    private double lastCenterX = 0d;
    private double lastCenterY = 0d;

    private double targetOffsetX = 0d;
    private double targetOffsetY = 0d;

    private double viewportWidth = 0d;
    private double viewportHeight = 0d;

    private Timeline animator;
    private final Set<Runnable> panAnimationListeners = new HashSet<>();

    // TODO: Transfer this hard-coded value to a preference value
    // Number of times doSmoothScroll() should be called in order for the entire transition to take place
    final int animationSteps = 30;

    public Camera(double viewportWidth, double viewportHeight) {
        this(0d, 0d, viewportWidth, viewportHeight);
    }

    public Camera(double offsetX, double offsetY, double viewportWidth, double viewportHeight) {
        setOffset(offsetX, offsetY);
        setViewportSize(viewportWidth, viewportHeight);
    }

    /**
     * Sets the target item to center on. The camera will pan to the target location over time.
     * To respond to panning animation intermediate-step events, subscribe to the pan animation listener 
     * using {@link #addPanAnimationListener(Runnable)}.
     *
     * @param centeredItem Element to center on
     * @param gridSize Size of each grid in the tree structure, can be obtained from {@link GameTreeElementSize#getGridSize()}
     */
    public void setCenterElementWithAnimation(TreeElement centeredItem, Dimension2D gridSize) {
        int gridX = centeredItem.getLogicalX();
        int gridY = centeredItem.getLogicalY();

        double centerX = gridX * gridSize.getWidth() + gridSize.getWidth() / 2;
        double centerY = gridY * gridSize.getHeight() + gridSize.getHeight() / 2;
        setCenterOnCoordinateWithAnimation(centerX, centerY);
    }

    /**
     * Set the point to center on. The camera will pan to the target location over time.
     * To respond to panning animation intermediate-step events, subscribe to the pan animation listener
     * using {@link #addPanAnimationListener(Runnable)}.
     *
     * @param centerX X position to center on
     * @param centerY Y position to center on
     */
    public void setCenterOnCoordinateWithAnimation(double centerX, double centerY) {
        setCenterOnCoordinate(centerX, centerY);
        startAnimation();
    }

    /**
     * Set the point to center on. The camera will be updated immediately without any animation.
     * A panning animation event will still be fired.
     *
     * @param centerX X position to center on
     * @param centerY Y position to center on
     */
    public void setCenterOnCoordinateImmediately(double centerX, double centerY) {
        setCenterOnCoordinate(centerX, centerY);

        // Method above should calculate target offset for us
        this.offsetX = targetOffsetX;
        this.offsetY = targetOffsetY;
        panAnimationListeners.forEach(Runnable::run);
    }

    private void setCenterOnCoordinate(double centerX, double centerY) {
        this.targetOffsetX = -(centerX - viewportWidth / 2);
        this.targetOffsetY = -(centerY - viewportHeight / 2);

        this.stepSizeX = (targetOffsetX - offsetX) / animationSteps;
        this.stepSizeY = (targetOffsetY - offsetY) / animationSteps;

        this.lastCenterX = centerX;
        this.lastCenterY = centerY;
    }

    /**
     * Sets the canvas size for the current tree component.
     *
     * @param viewportWidth Width of the tree canvas
     * @param viewportHeight Height of the tree canvas
     */
    public void setViewportSize(double viewportWidth, double viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;

        // Should be expected component behaviour, don't animate it.
        setCenterOnCoordinateImmediately(lastCenterX, lastCenterY);
    }

    /**
     * Performs one tick of scrolling if the viewport is currently not centered on the target item.
     * Otherwise, do nothing. The number of invocations required to fully pan to the target location is
     * dictated by {@link #animationSteps}.
     */
    public void doSmoothScroll() {
        if (!isCenteredOnTarget()) {
            offsetX += stepSizeX;
            offsetY += stepSizeY;
        }
    }

    /**
     *
     * @return true if the camera is currently centered on the target item.
     */
    public boolean isCenteredOnTarget() {
        return ComparisonUtilities.doubleEquals(targetOffsetX, offsetX)
                && ComparisonUtilities.doubleEquals(targetOffsetY, offsetY);
    }

    public void setOffset(double x, double y) {
        this.offsetX = x;
        this.offsetY = y;

        panAnimationListeners.forEach(Runnable::run);
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void reset() {
        this.offsetX = 0;
        this.offsetY = 0;
    }

    public void addPanAnimationListener(Runnable listener) {
        panAnimationListeners.add(listener);
    }

    public void removePanAnimationListener(Runnable listener) {
        panAnimationListeners.remove(listener);
    }

    private void startAnimation() {
        animator = new Timeline(new KeyFrame(new Duration(10), "Pan Tick", (event) -> {
            doSmoothScroll();
            panAnimationListeners.forEach(Runnable::run);
        }));

        animator.setCycleCount(animationSteps);
        animator.play();
    }

}
