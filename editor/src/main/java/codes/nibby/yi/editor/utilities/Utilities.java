package codes.nibby.yi.editor.utilities;

/**
 * A collection of miscellaneous utilities.
 */
public final class Utilities {

    public static final double EPSILON = 0.000005d;

    private Utilities() { }

    /**
     * Tests whether two double values are roughly equal, accounting for an accepted amount of precision loss.
     * The default imprecision value, epsilon, is {@link #EPSILON}.
     *
     * @param d1 First double value to compare
     * @param d2 Second double value to compare
     * @return true if the difference between the two values are within the range of tolerated imprecision, false otherwise.
     */
    public static boolean doubleEquals(double d1, double d2) {
        return doubleEquals(d1, d2, EPSILON);
    }

    /**
     * Tests whether two double values are roughly equal, accounting for an accepted amount of precision loss.
     * The default imprecision value, epsilon, is {@link #EPSILON}.
     *
     * @param d1 First double value to compare
     * @param d2 Second double value to compare
     * @param epsilon Acceptable threshold of imprecision, represented as the absolute difference between the two numbers.
     * @return true if the difference between the two values are within the range of tolerated imprecision, false otherwise.
     */
    public static boolean doubleEquals(double d1, double d2, double epsilon) {
        return Math.abs(d1 - d2) < epsilon;
    }

}
