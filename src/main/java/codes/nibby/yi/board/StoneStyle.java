package codes.nibby.yi.board;

public enum StoneStyle {

    PLAIN,
    FROM_THEME,
    BICONVEX_CERAMIC,
    SINGLE_CONVEX;

    public static StoneStyle parse(String string) {
        for (StoneStyle value : StoneStyle.values()) {
            if (value.name().equals(string.trim().toUpperCase()))
                return value;
        }
        return null;
    }
}
