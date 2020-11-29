package yi.editor.settings;

import javafx.scene.input.KeyCombination;
import org.json.JSONObject;
import yi.editor.framework.accelerator.EditorAcceleratorManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class EditorAcceleratorSettings extends EditorSettingsModule {

    private final String settingsFile;

    EditorAcceleratorSettings(String settingsFile) {
        this.settingsFile = settingsFile;
    }

    @Override
    public void load() {
        try {
            Optional<JSONObject> settingsWrapper = EditorSettings.readJSON(settingsFile);
            if (settingsWrapper.isEmpty()) {
                save(); // Just create the file with default settings
            } else {
                loadFromJson(settingsWrapper.get());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Sample format:

        {
            "<identifier>" : "<accelerator name>"
            ...
        }
     */
    private void loadFromJson(JSONObject settings) {
        for (String acceleratorId : settings.keySet()) {
            loadAccelerator(settings, acceleratorId);
        }
    }

    private void loadAccelerator(JSONObject settings, String acceleratorId) {
        String acceleratorName = settings.getString(acceleratorId);
        var combination = KeyCombination.valueOf(acceleratorName);
        EditorAcceleratorManager.setAcceleratorKeyCombination(acceleratorId, combination);
    }

    @Override
    public void save() {
        var settings = new JSONObject();
        for (EditorAcceleratorManager.Accelerator accelerator : EditorAcceleratorManager.getAllAccelerators().values()) {
            settings.put(accelerator.getId(), accelerator.getKeyCombination().getName());
        }

        Path file = Paths.get(settingsFile);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(settings.toString(4));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}