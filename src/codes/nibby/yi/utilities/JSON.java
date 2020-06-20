package codes.nibby.yi.utilities;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class JSON {

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

}
