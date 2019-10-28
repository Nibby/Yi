package codes.nibby.yi.game;

/**
 * Represents a board annotation (numbers, label, triangle, circle, square etc.)
 */
public final class Markup {

    // Starting point (common)
    private int x1, y1;
    // End point (for arrows / lines)
    private int x2, y2;
    private MarkupType type;
    private String arguments;

    private Markup() {
    }

    // TODO not implemented
    public static Markup parseSgf(int markType, String markerData) {
        System.out.println(markType + " => " + markerData);
        return null;
    }

    public static Markup triangle(int x, int y) {
        return basicMarkup(MarkupType.TRIANGLE, x, y);
    }

    public static Markup circle(int x, int y) {
        return basicMarkup(MarkupType.CIRCLE, x, y);
    }

    public static Markup square(int x, int y) {
        return basicMarkup(MarkupType.SQUARE, x, y);
    }

    public static Markup cross(int x, int y) {
        return basicMarkup(MarkupType.CROSS, x, y);
    }

    public static Markup label(int x, int y, String text) {
        Markup markup = basicMarkup(MarkupType.LABEL, x, y);
        markup.setArguments(text);
        return markup;
    }

    private static Markup basicMarkup(MarkupType type, int x, int y) {
        Markup markup = new Markup();
        markup.setType(type);
        markup.setX1(x);
        markup.setY1(y);
        return markup;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public MarkupType getType() {
        return type;
    }

    public void setType(MarkupType type) {
        this.type = type;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public void render(double x, double y, double size) {
        // TODO implement later
    }


}
