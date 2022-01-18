package codes.nibby.yi.app.utilities;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

public final class JSON {

    private JSON() {
        // Utility class, no instantiation
    }

    /**
     * Parses a file into {@link JSONObject}.
     *
     * @param jsonFilePath Path to JSON file
     * @return A {@link JSONObject} representation of the file
     * @throws IOException See {@link Files#newBufferedReader(Path)}}
     */
    public static JSONObject read(Path jsonFilePath) throws IOException {
        StringBuilder jsonSource = new StringBuilder();
        BufferedReader reader = Files.newBufferedReader(jsonFilePath, Charset.defaultCharset());
        reader.lines().forEach(line -> jsonSource.append(line).append("\n"));
        reader.close();
        return new JSONObject(jsonSource.toString());
    }

    public static JSONObject read(InputStream jsonFileInputStream) {
        StringBuilder jsonSource = new StringBuilder();
        Scanner scanner = new Scanner(jsonFileInputStream);
        while (scanner.hasNextLine()) {
            jsonSource.append(scanner.nextLine()).append("\n");
        }
        scanner.close();
        return new JSONObject(jsonSource.toString());
    }

    /**
     * Retrieves a string property from the JSON object.
     *
     * @param json The JSON object to retrieve value from
     * @param key The key to retrieve
     * @return The corresponding value for the key, or {@link Optional#empty()} if the key doesn't exist.
     */
    public static Optional<String> getString(JSONObject json, String key) {
        try {
            return Optional.of(json.getString(key));
        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves a string property from the JSON object.
     *
     * @param json The JSON object to retrieve value from
     * @param key The key to retrieve
     * @param defaultValue The value to return if no value exists with that key
     * @return The corresponding value for the key
     */
    public static String getString(JSONObject json, String key, @NotNull String defaultValue) {
        Objects.requireNonNull(defaultValue, "Default value must not be null");

        try {
            return json.getString(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(@NotNull JSONObject json, @NotNull String key, boolean defaultValue) {
        try {
            return json.getBoolean(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }
}
