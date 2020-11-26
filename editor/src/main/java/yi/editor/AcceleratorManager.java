package yi.editor;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import org.jetbrains.annotations.NotNull;
import yi.component.YiScene;
import yi.component.i18n.TextResource;
import yi.component.utilities.GuiUtilities;
import yi.component.utilities.KeyModifier;
import yi.component.utilities.SystemUtilities;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages a list of shortcut-keys that can be triggered across the application,
 * regardless of the currently focused component. These actions should be reserved
 * for the most common operations only, such as save, new document etc.
 * <p/>
 * These accelerators must be installed once for each new {@link Scene} through
 * {@link #installGlobalAccelerators(YiScene)}, ideally prior to setting the scene visible.
 */
public final class AcceleratorManager {

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final Map<String, Accelerator> ALL_ACCELERATORS = new HashMap<>();

    private UndoSystemHandler undoSystem;

    public static void initializeAll() {
        if (!INITIALIZED.get()) {
            Global.initialize();
            INITIALIZED.set(true);
        }
    }

    private static final class Global {

        private Global() {
            // Constant class, no instantiation
        }

        public static void initialize() {
            createUndo();
            createRedo();
            createPerspectiveAccelerators();
        }

        private static void createPerspectiveAccelerators() {
            new Accelerator(AcceleratorId.TOGGLE_PERSPECTIVE_REVIEW, Text.TOGGLE_PERSPECTIVE_REVIEW, KeyCode.E, new KeyModifier[] { KeyModifier.SHORTCUT });
            new Accelerator(AcceleratorId.TOGGLE_PERSPECTIVE_COMPACT, Text.TOGGLE_PERSPECTIVE_COMPACT, KeyCode.W, new KeyModifier[] { KeyModifier.SHORTCUT });
        }

        private static void createUndo() {
            new Accelerator(AcceleratorId.UNDO, Text.UNDO, KeyCode.Z, new KeyModifier[] {KeyModifier.SHORTCUT});
        }

        private static void createRedo() {
            if (SystemUtilities.isMac()) {
                new Accelerator(AcceleratorId.REDO, Text.REDO, KeyCode.Z, new KeyModifier[]{KeyModifier.SHORTCUT, KeyModifier.SHIFT});
            } else {
                new Accelerator(AcceleratorId.REDO, Text.REDO, KeyCode.Y, new KeyModifier[]{KeyModifier.SHORTCUT});
            }
        }
    }

    /**
     * Installs all global accelerator hotkeys for the scene.
     *
     * @param scene Scene to have global accelerators applied.
     */
    void installGlobalAccelerators(@NotNull YiScene scene) {
        installUndoSystemAccelerators(scene);
    }

    private void installUndoSystemAccelerators(@NotNull YiScene scene) {
        Runnable undoAction = () -> { if (undoSystem != null) undoSystem.requestUndo(); };
        Runnable redoAction = () -> { if (undoSystem != null) undoSystem.requestRedo(); };

        install(scene, AcceleratorId.UNDO, undoAction);
        install(scene, AcceleratorId.REDO, redoAction);
    }

    void setUndoSystemHandler(@NotNull UndoSystemHandler undoSystem) {
        this.undoSystem = Objects.requireNonNull(undoSystem);
    }

    private void install(@NotNull Scene scene, AcceleratorId acceleratorId, Runnable action) {
        scene.getAccelerators().put(getAccelerator(acceleratorId).getKeyCombination(), action);
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
     * Responds to the undo and redo action requests triggered by the global accelerator
     * hotkey.
     */
    interface UndoSystemHandler {
        void requestUndo();
        void requestRedo();
    }

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

            ALL_ACCELERATORS.put(getId(), this);
        }

        public String getId() {
            return id.getId();
        }

        public String getName() {
            return name.getLocalisedText();
        }

        public KeyCombination getKeyCombination() {
            if (keyCombination != null) {
                return keyCombination;
            }

            keyCombination = GuiUtilities.getKeyCombination(keyCode, modifiers);
            return keyCombination;
        }

        void setKeyCombination(@NotNull KeyCombination newCombination) {
            this.keyCombination = Objects.requireNonNull(newCombination);
        }
    }

}
