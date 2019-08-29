package codes.nibby.yi.editor.perspective;

/**
 * A predefined set of component layouts in the game editor window to help the user
 * achieve a specific purpose.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public enum Perspective {

    /**
     * The most minimal, compact setting.
     * Hides all but the go board and a few essential controls.
     *
     * Usage: viewing, simple click-once operations
     */
    SIMPLE("toolbar.perspective.simple"),

    /**
     * Usage: record editing
     */
    EDITOR("toolbar.perspective.editor"),

//
//    /**
//     * Usage: AI analysis
//     */
//    ANALYSIS("toolbar.perspective.analysis"),
//
//    /**
//     * A planned mode for now,
//     * it is similar to the SIMPLE mode, but with more eye-candy
//     * and controls to automate playback and game tree exploration.
//     *
//     */
//    PRESENTER("toolbar.perspective.presenter")
    ;

    private String textKey;

    Perspective(String textKey) {
        this.textKey = textKey;
    }

    public String getTextKey() {
        return textKey;
    }

    public static Perspective parse(String string) {
        for (Perspective p : Perspective.values()) {
            if (p.name().equals(string.trim().toUpperCase()))
                return p;
        }
        return null;
    }
}
