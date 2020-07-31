package yi.component.utilities;

import javafx.scene.input.KeyEvent;

public final class GuiUtilities {

    private GuiUtilities() {

    }

    /**
     * The main action key is {@code command} aka meta on macOS, otherwise {@code ctrl} on other
     * operating systems.
     *
     * @return true if the native action key has been pressed.
     */
    public static boolean isActionKeyDown(KeyEvent e) {
        return SystemUtilities.isMac() ? e.isMetaDown() : e.isControlDown();
    }

}
