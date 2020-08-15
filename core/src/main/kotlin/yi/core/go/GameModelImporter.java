package yi.core.go;

// This class is written in Java because it is a utility class, and
// Kotlin interop will force usage of .Companion or append Kt as type suffix.

import yi.core.go.docformat.FileFormat;
import yi.core.go.docformat.FileFormatHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides the functionality to load a {@link GameModel} from an
 * external source such as files stored on local disk.
 * <p/>
 * To export an existing model, use {@link GameModelExporter}.
 */
public final class GameModelImporter {

    /**
     * Parses the an internal file in the classpath as a {@link GameModel}.
     *
     * @param fileResourceUrl URL of the file resource in the classpath, starting with a leading backslash.
     * @param format Format of the game file.
     * @param resourceClass Reference class to supply the classpath used to find the resource.
     * @return Parsed game model from the specified file.
     *
     * @throws BadFormatException If the file content is corrupted or does not comply with the current format.
     */
    public static GameModel fromInternalResources(String fileResourceUrl, FileFormat format, Class<?> resourceClass) throws BadFormatException {
        var handler = format.getHandler();
        var inputStream = resourceClass.getResourceAsStream(fileResourceUrl);
        return handler.load(inputStream);
    }

    /**
     * Parses a given file as a {@link GameModel}, automatically detecting the
     * format to use based on its extension or -- if no extension provided --
     * a quick scan of its contents.
     * <p/>
     * This method fails if its contents appear to
     * match more than one file format and will throw an {@link UnsupportedOperationException}.
     * <p/>
     * The file can be any of the format supported by the Yi library. See
     * {@link FileFormat} for a comprehensive list.
     *
     * @param filePath The path to the game model data file.
     * @return {@link GameModel} that represents the game data in the file.
     *
     * @throws BadFormatException If the file is corrupted, or a fatal file format error occurs.
     * @throws UnsupportedOperationException If the file format is ambiguous.
     * Use a {@link #fromFile(Path, FileFormat) format-specific} version to resolve this issue.
     * @throws IOException Issues with identifying or setting up the input stream for the file.
     */
    public static GameModel fromFile(Path filePath) throws BadFormatException, IOException {
        var name = filePath.getFileName().toString();
        var extensionDot = name.lastIndexOf(".");

        Set<FileFormat> formats;
        if (extensionDot == -1) {
            // File has no extension, scan it.
            var inputStream = Files.newInputStream(filePath, StandardOpenOption.READ);
            formats = getRecognizedFormats(format -> format.getHandler().isLikelyLoadable(inputStream));
        } else {
            var extension = name.substring(extensionDot+1);
            formats = getRecognizedFormats(format -> format.isSupportedFormat(extension));
        }

        if (formats.size() == 1) {
            var format = formats.iterator().next();
            return _fromFile(filePath, format.getHandler());
        } else {
            throw new UnsupportedOperationException("File conforms to more than 1 format: " + formats);
        }
    }

    /**
     * Parses a given file as a {@link GameModel} using a specified file format.
     *
     * @param filePath The path to the game model data file.
     * @param format Format of the game file.
     * @return {@link GameModel} that represents the game data in the file.
     *
     * @throws BadFormatException If the file is corrupted, or a fatal file format error occurs.
     * @throws IOException Issues with identifying or setting up the input stream for the file.
     */
    public static GameModel fromFile(Path filePath, FileFormat format) throws BadFormatException, IOException {
        return _fromFile(filePath, format.getHandler());
    }

    private static GameModel _fromFile(Path filePath, FileFormatHandler handler) throws BadFormatException, IOException {
        var inputStream = Files.newInputStream(filePath, StandardOpenOption.READ);
        return handler.load(inputStream);
    }

    private static Set<FileFormat> getRecognizedFormats(Function<FileFormat, Boolean> formatEvaluator) {
        return Arrays.stream(FileFormat.values())
                .filter(formatEvaluator::apply)
                .collect(Collectors.toSet());
    }
}
