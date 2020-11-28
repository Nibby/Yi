package yi.editor;

import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import org.jetbrains.annotations.NotNull;
import yi.common.i18n.TextResource;
import yi.common.utilities.GuiUtilities;
import yi.component.KeyModifier;
import yi.common.utilities.SystemUtilities;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages all the shortcut-keys that can be triggered in the application. Each shortcut
 * is mapped to an {@link Accelerator} instance which is created within the implementation
 * of this class.
 * <p/>
 * To assign a shortcut key to supported Fx components, retrieve the accelerator instance
 * using {@link #getAccelerator(AcceleratorId)} with the desired id, then invoke
 * {@link Accelerator#install(MenuItem)} or one of its overloaded variants.
 */
public final class AcceleratorManager {

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final Map<String, Accelerator> ALL_ACCELERATORS = new HashMap<>();

    private AcceleratorManager() {
        // Utility class, not to be instantiated
    }

    public static void initializeAll() {
        if (!INITIALIZED.get()) {
            createUndoRedo();
            createPerspectiveAccelerators();
            register(new Accelerator(AcceleratorId.NEW_GAME, TextKeys.MENUITEM_NEW_GAME, KeyCode.N, new KeyModifier[] { KeyModifier.SHORTCUT }));
            register(new Accelerator(AcceleratorId.OPEN_GAME, TextKeys.MENUITEM_OPEN_GAME, KeyCode.O, new KeyModifier[] { KeyModifier.SHORTCUT }));
            register(new Accelerator(AcceleratorId.SAVE_GAME, TextKeys.MENUITEM_SAVE_GAME, KeyCode.S, new KeyModifier[] { KeyModifier.SHORTCUT }));
            register(new Accelerator(AcceleratorId.SAVE_AS_GAME, TextKeys.MENUITEM_SAVE_AS_GAME, KeyCode.S, new KeyModifier[] { KeyModifier.SHORTCUT, KeyModifier.SHIFT }));

            register(new Accelerator(AcceleratorId.TOGGLE_BOARD_COORDINATES, TextKeys.MENUITEM_TOGGLE_COORDINATES, KeyCode.C, new KeyModifier[] { KeyModifier.SHORTCUT, KeyModifier.SHIFT }));

            INITIALIZED.set(true);
        }
    }

    private static void createPerspectiveAccelerators() {
        register(new Accelerator(AcceleratorId.TOGGLE_PERSPECTIVE_REVIEW, TextKeys.TOGGLE_PERSPECTIVE_REVIEW, KeyCode.E, new KeyModifier[] { KeyModifier.SHORTCUT }));
        register(new Accelerator(AcceleratorId.TOGGLE_PERSPECTIVE_COMPACT, TextKeys.TOGGLE_PERSPECTIVE_COMPACT, KeyCode.W, new KeyModifier[] { KeyModifier.SHORTCUT }));
    }

    private static void createUndoRedo() {
        register(new Accelerator(AcceleratorId.UNDO, TextKeys.UNDO, KeyCode.Z, new KeyModifier[] {KeyModifier.SHORTCUT}));
        if (SystemUtilities.isMac()) {
            register(new Accelerator(AcceleratorId.REDO, TextKeys.REDO, KeyCode.Z, new KeyModifier[]{KeyModifier.SHORTCUT, KeyModifier.SHIFT}));
        } else {
            register(new Accelerator(AcceleratorId.REDO, TextKeys.REDO, KeyCode.Y, new KeyModifier[]{KeyModifier.SHORTCUT}));
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

    public static Accelerator getAccelerator(AcceleratorId id) {
        var accelerator = getAccelerator(id.getId());
        if (accelerator.isEmpty()) {
            throw new IllegalStateException("No accelerator mapped to id '" +
                    id + "' despite there being an AcceleratorId entry for it.");
        }
        return accelerator.get();
    }

    /**
     * Represents a {@link KeyCombination} for a particular feature or action.
     * <p/>
     * To set the accelerator for a supported Fx component, use {@link #install(MenuItem)}
     * or one of its overloaded variants.
     */
    public static final class Accelerator {

        private final AcceleratorId id;
        private final TextResource name;
        private final KeyModifier[] modifiers;
        private final KeyCode keyCode;
        private KeyCombination keyCombination = null;

        Accelerator(AcceleratorId id, TextResource name, KeyCode keyCode, KeyModifier[] modifiers) {
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
         * values in {@link AcceleratorId}.
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
