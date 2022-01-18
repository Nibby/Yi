package codes.nibby.yi.app.framework;

import javafx.scene.input.KeyCode;
import codes.nibby.yi.app.components.Accelerator;
import codes.nibby.yi.app.i18n.TextResource;
import codes.nibby.yi.app.utilities.SystemUtilities;

import java.util.function.Function;

/**
 * List of all the shortcut-keys that can be triggered in the application. Each shortcut
 * is mapped to an {@link Accelerator} instance which is created within the implementation
 * of this class.
 * <p/>
 * To assign a shortcut key to supported Fx components, use the following:
 * <pre>
 *     var accelerator = EditorAccelerator.SOME_ACCELERATOR.getAccelerator();
 *     accelerator.install(menuItem); // Or one of the other .install variants()
 * </pre>
 * or one of its overloaded variants.
 */
public enum AppAccelerator {

    UNDO("undo", AppText.UNDO, KeyCode.Z, new KeyModifier[] {KeyModifier.SHORTCUT}),
    REDO("redo", (id) -> {
        Accelerator accelerator;
        if (SystemUtilities.isMac()) {
            accelerator = new Accelerator(id, AppText.REDO, KeyCode.Z, new KeyModifier[]{KeyModifier.SHORTCUT, KeyModifier.SHIFT});
        } else {
            accelerator = new Accelerator(id, AppText.REDO, KeyCode.Y, new KeyModifier[]{KeyModifier.SHORTCUT});
        }
        return accelerator;
    }),

    TOGGLE_PERSPECTIVE_COMPACT("togglePerspectiveCompact", AppText.TOGGLE_PERSPECTIVE_COMPACT, KeyCode.W, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOGGLE_PERSPECTIVE_EDIT("togglePerspectiveEdit", AppText.TOGGLE_PERSPECTIVE_EDIT, KeyCode.E, new KeyModifier[] { KeyModifier.SHORTCUT }),

    TOGGLE_BOARD_COORDINATES("toggleBoardCoordinates", AppText.MENUITEM_TOGGLE_COORDINATES, KeyCode.C, new KeyModifier[] { KeyModifier.SHORTCUT, KeyModifier.SHIFT }),

    TOOL_PLAY_MOVE("toolPlayMove", AppText.TOOL_PLAY_MOVE, KeyCode.DIGIT1, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_TRIANGLE("toolTriangle", AppText.TOOL_TRIANGLE, KeyCode.DIGIT3, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_SQUARE("toolSquare", AppText.TOOL_SQUARE, KeyCode.DIGIT4, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_CIRCLE("toolCircle", AppText.TOOL_CIRCLE, KeyCode.DIGIT5, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_CROSS("toolCross", AppText.TOOL_CROSS, KeyCode.DIGIT2, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_LABEL_LETTER("toolLetter", AppText.TOOL_LABEL_LETTER, KeyCode.DIGIT6, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_LABEL_NUMBER("toolNumber", AppText.TOOL_LABEL_NUMBER, KeyCode.DIGIT7, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_LINE("toolLine", AppText.TOOL_LINE, KeyCode.DIGIT8, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_ARROW("toolArrow", AppText.TOOL_ARROW, KeyCode.DIGIT9, new KeyModifier[] { KeyModifier.SHORTCUT }),

    NEW_GAME("newGame", AppText.MENUITEM_NEW_GAME, KeyCode.N, new KeyModifier[] { KeyModifier.SHORTCUT }),
    NEW_WINDOW("newWindow", AppText.MENUITEM_NEW_WINDOW, KeyCode.N, new KeyModifier[] { KeyModifier.SHORTCUT, KeyModifier.SHIFT }),
    OPEN_GAME("openGame", AppText.MENUITEM_OPEN_GAME, KeyCode.O, new KeyModifier[] { KeyModifier.SHORTCUT }),
    SAVE_GAME("saveGame", AppText.MENUITEM_SAVE_GAME, KeyCode.S, new KeyModifier[] { KeyModifier.SHORTCUT }),
    SAVE_AS_GAME("saveAsGame", AppText.MENUITEM_SAVE_AS_GAME, KeyCode.S, new KeyModifier[] { KeyModifier.SHORTCUT, KeyModifier.SHIFT }),

    PASS("pass", AppText.PASS, KeyCode.P, new KeyModifier[] { KeyModifier.SHORTCUT }),
    REMOVE_NODE("removeNode", AppText.REMOVE_NODE, KeyCode.BACK_SPACE, new KeyModifier[0]),

    TO_PREVIOUS_NODE("toPreviousNode", AppText.SAVE, KeyCode.UP, new KeyModifier[0]),
    TO_PREVIOUS_10_NODES("toPrevious10Nodes", AppText.TO_PREVIOUS_10_NODES, KeyCode.UP, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TO_ROOT_NODE("toRootNode", AppText.TO_ROOT_NODE, KeyCode.UP, new KeyModifier[] { KeyModifier.SHIFT, KeyModifier.SHORTCUT }),
    TO_NEXT_NODE("toNextNode", AppText.TO_NEXT_NODE, KeyCode.DOWN, new KeyModifier[0]),
    TO_NEXT_10_NODES("toNext10Nodes", AppText.TO_NEXT_10_NODES, KeyCode.DOWN, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TO_VARIATION_END("toVariationEnd", AppText.TO_VARIATION_END, KeyCode.DOWN, new KeyModifier[] { KeyModifier.SHIFT, KeyModifier.SHORTCUT }),

    EDIT_GAME_INFO("editGameInfo", AppText.EDIT_GAME_INFO, KeyCode.I, new KeyModifier[] { KeyModifier.SHORTCUT }),

    TEST_ACCEL_1("testAccel1", AppText.EMPTY, KeyCode.DIGIT1, new KeyModifier[] { KeyModifier.CTRL, KeyModifier.ALT, KeyModifier.SHIFT }),
    TEST_ACCEL_2("testAccel2", AppText.EMPTY, KeyCode.DIGIT2, new KeyModifier[] { KeyModifier.CTRL, KeyModifier.ALT, KeyModifier.SHIFT }),

    ;

    private final String id;
    private final Accelerator accelerator;

    AppAccelerator(String id, Function<String, Accelerator> acceleratorSupplier) {
        this.id = id;
        this.accelerator = acceleratorSupplier.apply(id);
    }

    AppAccelerator(String id,
                   TextResource name,
                   KeyCode keyCode,
                   KeyModifier[] modifiers) {
        this(id, (proxyId) -> new Accelerator(id, name, keyCode, modifiers));
    }

    public String getId() {
        return id;
    }

    public Accelerator getAccelerator() {
        return accelerator;
    }

    public static Accelerator getAccelerator(AppAccelerator id) {
        var idAsString = id.getId();
        return Accelerator.getAccelerator(idAsString).orElseThrow();
    }
}
