package yi.component.gametree;

import yi.core.go.GoGameModel;

final class Viewport {

    private double offsetX;
    private double offsetY;

    public Viewport() {
        this(0d, 0d);
    }

    public Viewport(double offsetX, double offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }
}
