package yi.component.shared.component;

import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * A JavaFx container that automatically resizes internal canvases when the container size has changed.
 */
public final class CanvasContainer extends Pane {

    private final List<Canvas> canvas;
    private final List<Consumer<Dimension2D>> canvasSizeListeners = new ArrayList<>();

    /**
     * Creates a container for one internal child canvas. This canvas will be the same size as the
     * bounds of the container minus insets.
     *
     * @param canvas Child canvas managed by this container,
     */
    public CanvasContainer(Canvas canvas) {
        this(Collections.singletonList(canvas));
    }

    /**
     * Creates a container for multiple child canvas. These canvas will share the same size as the
     * bounds of the container minus insets. The canvas will be laid out in list iteration order,
     * where the highest indexed item is displayed above all others.
     *
     * @param canvas Collection of canvases managed by this container.
     */
    public CanvasContainer(Collection<? extends Canvas> canvas) {
        this.canvas = List.copyOf(canvas);
        this.getChildren().addAll(canvas);
    }

    /**
     * Add a listener that is notified each time after the container (and consequently its managed
     * components) have been resized.
     *
     * @param listener The listener to be notified of the new container size.
     */
    public void addSizeUpdateListener(Consumer<Dimension2D> listener) {
        canvasSizeListeners.add(listener);
    }

    public void removeSizeUpdateListener(Consumer<Dimension2D> listener) {
        canvasSizeListeners.remove(listener);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        resizeCanvas();
    }

    private void resizeCanvas() {
        final double x = snappedLeftInset();
        final double y = snappedTopInset();
        final double w = snapSizeX(getWidth()) - x - snappedRightInset();
        final double h = snapSizeY(getHeight()) - y - snappedBottomInset();

        canvas.forEach(c -> {
            c.setLayoutX(x);
            c.setLayoutY(y);
            c.setWidth(w);
            c.setHeight(h);
        });

        var size = new Dimension2D(w, h);
        canvasSizeListeners.forEach(listener -> listener.accept(size));
    }
}
