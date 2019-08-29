package codes.nibby.yi.editor.layout;

/**
 * A predefined set of component layouts in the game editor window to help the user
 * achieve a specific purpose.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public enum LayoutType {

    /**
     * Usage: record editing
     */
    EDIT("layouts.edit"),


    /**
     * Usage: AI analysis
     */
    ANALYSIS("layouts.analysis"),

    /**
     * A planned mode for now,
     * it is similar to the SIMPLE mode, but with more eye-candy
     * and controls to automate playback and game tree exploration.
     *
     */
    PRESENTER("layouts.presenter")
    ;

    private String textKey;

    LayoutType(String textKey) {
        this.textKey = textKey;
    }

    public String getTextKey() {
        return textKey;
    }

    public static LayoutType parse(String string) {
        for (LayoutType p : LayoutType.values()) {
            if (p.name().equals(string.trim().toUpperCase()))
                return p;
        }
        return null;
    }
}
