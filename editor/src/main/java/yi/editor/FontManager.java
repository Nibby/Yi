package yi.editor;

import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FontManager {

    public static void loadBundledFonts() {
        final String FONT_RESOURCE_DIR = "/fonts/";
        URI fontDirectoryUri;

        try {
            fontDirectoryUri = FontManager.class.getResource(FONT_RESOURCE_DIR).toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Malformed font resource directory value: " +
                    "\"" + FONT_RESOURCE_DIR + "\"");
        }

        var fontDirectoryAsPath = Paths.get(fontDirectoryUri);

        try (DirectoryStream<Path> files = Files.newDirectoryStream(fontDirectoryAsPath)) {
            for (Path file : files) {
                if (isFontFile(file)) {
                    loadFont(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadFont(Path file) throws IOException {
        var inputStream = Files.newInputStream(file);
        Font.loadFont(inputStream, 12d);
    }

    private static boolean isFontFile(Path file) {
        return Files.isRegularFile(file)
                 && file.getFileName().toString().toLowerCase().endsWith(".ttf");
    }
}
