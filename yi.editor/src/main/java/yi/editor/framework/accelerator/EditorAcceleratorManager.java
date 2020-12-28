package yi.editor.framework.accelerator;

import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import org.jetbrains.annotations.NotNull;
import yi.component.shared.component.KeyModifier;
import yi.component.shared.component.YiScene;
import yi.component.shared.i18n.TextResource;
import yi.component.shared.utilities.GuiUtilities;
import yi.component.shared.utilities.SystemUtilities;
import yi.editor.framework.EditorHelper;
import yi.editor.components.EditorTextResources;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages all the shortcut-keys that can be triggered in the application. Each shortcut
 * is mapped to an {@link Accelerator} instance which is created within the implementation
 * of this class.
 * <p/>
 * To assign a shortcut key to supported Fx components, use {@link #install(EditorAcceleratorId, MenuItem)}
 * or one of its overloaded variants.
 */
public final class EditorAcceleratorManager {

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final Map<String, Accelerator> ALL_ACCELERATORS = new HashMap<>();

    private EditorAcceleratorManager() {
        // Utility class, not to be instantiated
    }


    public static void initializeAll() {
        if (!INITIALIZED.get()) {
            createUndoRedo();
            createPerspectiveAccelerators();
            createEditToolAccelerators();
            register(new Accelerator(EditorAcceleratorId.NEW_GAME, EditorTextResources.MENUITEM_NEW_GAME, KeyCode.N, new KeyModifier[] { KeyModifier.SHORTCUT }));
            register(new Accelerator(EditorAcceleratorId.NEW_WINDOW, EditorTextResources.MENUITEM_NEW_WINDOW, KeyCode.N, new KeyModifier[] { KeyModifier.SHORTCUT, KeyModifier.SHIFT }));
            register(new Accelerator(EditorAcceleratorId.OPEN_GAME, EditorTextResources.MENUITEM_OPEN_GAME, KeyCode.O, new KeyModifier[] { KeyModifier.SHORTCUT }));
            register(new Accelerator(EditorAcceleratorId.SAVE_GAME, EditorTextResources.MENUITEM_SAVE_GAME, KeyCode.S, new KeyModifier[] { KeyModifier.SHORTCUT }));
            register(new Accelerator(EditorAcceleratorId.SAVE_AS_GAME, EditorTextResources.MENUITEM_SAVE_AS_GAME, KeyCode.S, new KeyModifier[] { KeyModifier.SHORTCUT, KeyModifier.SHIFT }));
            register(new Accelerator(EditorAcceleratorId.TOGGLE_BOARD_COORDINATES, EditorTextResources.MENUITEM_TOGGLE_COORDINATES, KeyCode.C, new KeyModifier[] { KeyModifier.SHORTCUT, KeyModifier.SHIFT }));

            if (EditorHelper.isRunningAsTest()) {
                var modifiers = new KeyModifier[] { KeyModifier.CTRL, KeyModifier.ALT, KeyModifier.SHIFT };
                register(new Accelerator(EditorAcceleratorId.TEST_ACCEL_1, EditorTextResources.EMPTY, KeyCode.DIGIT1, modifiers));
                register(new Accelerator(EditorAcceleratorId.TEST_ACCEL_2, EditorTextResources.EMPTY, KeyCode.DIGIT2, modifiers));
            }
            INITIALIZED.set(true);
        }
    }

    private static void createEditToolAccelerators() {
        register(new Accelerator(EditorAcceleratorId.TOOL_PLAY_MOVE, EditorTextResources.TOOL_PLAY_MOVE, KeyCode.DIGIT1, new KeyModifier[] { KeyModifier.SHORTCUT }));
        register(new Accelerator(EditorAcceleratorId.TOOL_CROSS, EditorTextResources.TOOL_CROSS, KeyCode.DIGIT2, new KeyModifier[] { KeyModifier.SHORTCUT }));
        register(new Accelerator(EditorAcceleratorId.TOOL_TRIANGLE, EditorTextResources.TOOL_TRIANGLE, KeyCode.DIGIT3, new KeyModifier[] { KeyModifier.SHORTCUT }));
        register(new Accelerator(EditorAcceleratorId.TOOL_SQUARE, EditorTextResources.TOOL_SQUARE, KeyCode.DIGIT4, new KeyModifier[] { KeyModifier.SHORTCUT }));
        register(new Accelerator(EditorAcceleratorId.TOOL_CIRCLE, EditorTextResources.TOOL_CIRCLE, KeyCode.DIGIT5, new KeyModifier[] { KeyModifier.SHORTCUT }));
        register(new Accelerator(EditorAcceleratorId.TOOL_LABEL_LETTER, EditorTextResources.TOOL_LABEL_LETTER, KeyCode.DIGIT6, new KeyModifier[] { KeyModifier.SHORTCUT }));
        register(new Accelerator(EditorAcceleratorId.TOOL_LABEL_NUMBER, EditorTextResources.TOOL_LABEL_NUMBER, KeyCode.DIGIT7, new KeyModifier[] { KeyModifier.SHORTCUT }));
        register(new Accelerator(EditorAcceleratorId.TOOL_LINE, EditorTextResources.TOOL_LINE, KeyCode.DIGIT8, new KeyModifier[] { KeyModifier.SHORTCUT }));
        register(new Accelerator(EditorAcceleratorId.TOOL_ARROW, EditorTextResources.TOOL_ARROW, KeyCode.DIGIT9, new KeyModifier[] { KeyModifier.SHORTCUT }));
    }

    private static void createPerspectiveAccelerators() {
        register(new Accelerator(EditorAcceleratorId.TOGGLE_PERSPECTIVE_REVIEW, EditorTextResources.TOGGLE_PERSPECTIVE_REVIEW, KeyCode.E, new KeyModifier[] { KeyModifier.SHORTCUT }));
        register(new Accelerator(EditorAcceleratorId.TOGGLE_PERSPECTIVE_COMPACT, EditorTextResources.TOGGLE_PERSPECTIVE_COMPACT, KeyCode.W, new KeyModifier[] { KeyModifier.SHORTCUT }));
    }

    private static void createUndoRedo() {
        register(new Accelerator(EditorAcceleratorId.UNDO, EditorTextResources.UNDO, KeyCode.Z, new KeyModifier[] {KeyModifier.SHORTCUT}));
        if (SystemUtilities.isMac()) {
            register(new Accelerator(EditorAcceleratorId.REDO, EditorTextResources.REDO, KeyCode.Z, new KeyModifier[]{KeyModifier.SHORTCUT, KeyModifier.SHIFT}));
        } else {
            register(new Accelerator(EditorAcceleratorId.REDO, EditorTextResources.REDO, KeyCode.Y, new KeyModifier[]{KeyModifier.SHORTCUT}));
        }
    }

    private static void register(@NotNull Accelerator accelerator) {
        Objects.requireNonNull(accelerator);
        ALL_ACCELERATORS.put(accelerator.getId(), accelerator);
    }

    public static Map<String, Accelerator> getAllAccelerators() {
        return Collections.unmodifiableMap(ALL_ACCELERATORS);
    }

    public static void setAcceleratorKeyCombination(String acceleratorId, KeyCombination keyCombination) {
        getAccelerator(acceleratorId).ifPresent(accelerator -> accelerator.setKeyCombination(keyCombination));
    }

    public static Optional<Accelerator> getAccelerator(String id) {
        return Optional.ofNullable(ALL_ACCELERATORS.get(id));
    }

    public static Accelerator getAccelerator(EditorAcceleratorId id) {
        var accelerator = getAccelerator(id.getId());
        if (accelerator.isEmpty()) {
            throw new IllegalStateException("No accelerator mapped to id '" +
                    id + "' despite there being an AcceleratorId entry for it.");
        }
        return accelerator.get();
    }

    /**
     * Sets the menu item with a shortcut key defined by an accelerator mapped to the
     * given id. This will overwrite any existing shortcut keys set on the menu item.
     *
     * @param id Unique ID of the accelerator to be installed.
     * @param menuItem Menu item component to have shortcut keys set.
     */
    public static void install(EditorAcceleratorId id, MenuItem menuItem) {
        getAccelerator(id).install(menuItem);
    }

    /**
     * Adds an additional shortcut key to trigger some action on the given scene.
     *
     * @param id Unique ID of the accelerator to be installed.
     * @param scene Scene to have shortcut keys added.
     * @param action Code to run when the shortcut key is active.
     */
    public static void install(EditorAcceleratorId id, YiScene scene, Runnable action) {
        getAccelerator(id).install(scene, action);
    }

    /**
     * Represents a {@link KeyCombination} for a particular feature or action.
     * <p/>
     * To set the accelerator for a supported Fx component, use {@link #install(MenuItem)}
     * or one of its overloaded variants.
     */
    public static final class Accelerator {

        private final EditorAcceleratorId id;
        private final TextResource name;
        private final KeyModifier[] modifiers;
        private final KeyCode keyCode;
        private KeyCombination keyCombination = null;

        Accelerator(EditorAcceleratorId id, TextResource name, KeyCode keyCode, KeyModifier[] modifiers) {
            if (ALL_ACCELERATORS.containsKey(id.getId())) {
                throw new IllegalStateException("Duplicated shortcut key AcceleratorId: " + id.getId());
            }

            this.id = id;
            this.name = Objects.requireNonNull(name, "Name must not be null");
            this.modifiers = Objects.requireNonNull(modifiers, "Modifiers must not be null");
            this.keyCode = Objects.requireNonNull(keyCode, "Key code must not be null");
        }

        /**
         * @return Internal unique identifier for the accelerator. Should be one of the
         * values in {@link EditorAcceleratorId}.
         */
        public String getId() {
            return id.getId();
        }

        /**
         * @return User-friendly description of the purpose of this accelerator.
         */
        public String getName() {
            return name.getLocalisedText();
        }

        /**
         * @return The Fx key combination represented by this accelerator.
         */
        public KeyCombination getKeyCombination() {
            if (keyCombination != null) {
                return keyCombination;
            }

            keyCombination = GuiUtilities.getKeyCombination(keyCode, modifiers);
            return keyCombination;
        }

        /**
         * Installs the shortcut key represented by this accelerator to the menu item.
         *
         * @param menuItem Item to have the shortcut key applied.
         */
        public void install(@NotNull MenuItem menuItem) {
            menuItem.acceleratorProperty().setValue(getKeyCombination());
        }

        /**
         * Adds a global shortcut key to the scene. This means the action will be executed
         * as long as the key combination is active, regardless of the currently focused
         * component within the scene.
         *
         * @param scene Scene to install shortcut key.
         * @param action Action to perform once the key combination is active.
         */
        public void install(@NotNull Scene scene, Runnable action) {
            scene.getAccelerators().put(getKeyCombination(), action);
        }

        void setKeyCombination(@NotNull KeyCombination newCombination) {
            this.keyCombination = Objects.requireNonNull(newCombination);
        }
    }
}
