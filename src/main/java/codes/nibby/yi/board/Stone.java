package codes.nibby.yi.board;

import javafx.scene.canvas.GraphicsContext;

import java.util.Random;

/**
 * Represents a renderable stone entity on the board.
 *
 * "A go program should be just as aesthetically pleasing as
 * it is functional. The zen atmosphere and the arrangement of
 * of lines, circles and squares are part of what makes
 * Go an enjoyable game."
 *
 * @author Kevin Yang
 * Created on 25 August 2019
 */
public class Stone implements IRenderable {

    public static final int NONE = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;

    /** Position on the go board. */
    private int x, y;

    /** Color of the stone. */
    private int color;

    /** The amount of wobbling left to perform. */
    private double wobble;
    private double wobbleMax;

    /** Wobble offset. */
    private double wobbleX, wobbleY;

    /*
        When fuzzy placement is turned on, stones can be bumped
        off its original placement. These are the parameters that
        imitate this effect.
     */
    /** Fuzzy placement offset. */
    protected double fuzzyX = 0, fuzzyY = 0;

    public Stone(int color, int x, int y) {
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public void onPlace(GameBoard board) {
        BoardMetrics metrics = board.getMetrics();
        nudge(metrics);
    }

    /**
     * Shifts the stone slightly around its intended origin.
     * This is to give a controlled 'messy' effect as if the stones
     * are played on a real board.
     *
     * @param metrics Board parameters.
     */
    public void nudge(BoardMetrics metrics) {
        int factor = (int) (Math.random() * 2) - 2;
        double margin = metrics.getStoneSize() / 20;
        fuzzyX = Math.random() * margin * factor;
        fuzzyY = Math.random() * margin * factor;
    }

    /**
     * If the stone is wobbling, this method will update
     * the stone aesthetics.
     */
    public void wobble() {
        if (wobble <= 0)
            return;

        // TODO: Reduce the amount of hard coded values
        Random r = new Random();
        double w = wobble / wobbleMax;
        wobbleX = r.nextDouble() * w;
        wobbleY = r.nextDouble() * w;
        wobble -= r.nextDouble() * 0.01d + 0.05d;
    }

    /**
     * Sets the amount of stone wobbling to be performed.
     *
     * @param wobble Amount of wobbling.
     */
    public void setWobble(double wobble) {
        this.wobble = wobble;
        this.wobbleMax = wobble;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getColor() {
        return color;
    }

    public boolean shouldWobble() {
        return wobble > 0d;
    }

    public double getWobbleX() {
        return wobbleX;
    }

    public double getWobbleY() {
        return wobbleY;
    }

    public double getFuzzyX() {
        return fuzzyX;
    }

    public double getFuzzyY() {
        return fuzzyY;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
