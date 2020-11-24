package yi.core.go

import yi.core.go.docformat.FileFormat
import yi.core.go.docformat.FileFormatHandler
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

/**
 * Provides the functionality to load a [GameModel] from an
 * external source such as files stored on local disk.
 *
 *
 * To export an existing model, use [GameModelExporter].
 */
object GameModelImporter {

    /**
     * Parses the an internal file in the classpath as a [GameModel].
     *
     * @param fileResourceUrl URL of the file resource in the classpath, starting with a
     * leading backslash.
     * @param format Format of the game file.
     * @param resourceClass Reference class to supply the classpath used to find the
     * resource.
     * @return Parsed game model from the specified file.
     *
     * @throws GameParseException If the file content is corrupted or does not comply with the current format.
     */
    @Throws(GameParseException::class)
    fun fromInternalResources(fileResourceUrl: String, format: FileFormat, resourceClass: Class<*>): GameModel {
        val handler = format.getHandler()
        val inputStream = resourceClass.getResourceAsStream(fileResourceUrl)
        return handler.doImport(inputStream)
    }

    /**
     * Parses a given file as a [GameModel], automatically detecting the
     * format to use based on its extension or -- if no extension provided --
     * a quick scan of its contents.
     *
     *
     * This method fails if its contents appear to
     * match more than one file format and will throw an [UnsupportedOperationException].
     *
     *
     * The file can be any of the format supported by the Yi library. See
     * [FileFormat] for a comprehensive list.
     *
     * @param filePath The path to the game model data file.
     * @return [GameModel] that represents the game data in the file.
     *
     * @throws GameParseException If the file is corrupted, or a fatal file format error occurs.
     * @throws UnsupportedOperationException If the file format is ambiguous.
     * Use a [format-specific][.fromFile] version to resolve this issue.
     * @throws IOException Issues with identifying or setting up the input stream for the file.
     */
    @Throws(GameParseException::class, IOException::class)
    fun fromFile(filePath: Path): GameModel {
        val name = filePath.fileName.toString()
        val extensionDot = name.lastIndexOf(".")
        val formats: Set<FileFormat>
        formats = if (extensionDot == -1) {
            // File has no extension, scan it.
            val inputStream = Files.newInputStream(filePath, StandardOpenOption.READ)
            getRecognizedFormats(Function { format: FileFormat -> format.getHandler().isLikelyLoadable(inputStream) })
        } else {
            val extension = name.substring(extensionDot + 1)
            getRecognizedFormats(Function { format: FileFormat -> format.isSupportedFormat(extension) })
        }
        return if (formats.size == 1) {
            val format = formats.iterator().next()
            _fromFile(filePath, format.getHandler())
        } else {
            throw UnsupportedOperationException("File conforms to more than 1 format: $formats")
        }
    }

    /**
     * Parses a given file as a [GameModel] using a specified file format.
     *
     * @param filePath The path to the game model data file.
     * @param format Format of the game file.
     * @return [GameModel] that represents the game data in the file.
     *
     * @throws GameParseException If the file is corrupted, or a fatal file format error occurs.
     * @throws IOException Issues with identifying or setting up the input stream for the file.
     */
    @Throws(GameParseException::class, IOException::class)
    fun fromFile(filePath: Path, format: FileFormat): GameModel {
        return _fromFile(filePath, format.getHandler())
    }

    @Throws(GameParseException::class, IOException::class)
    private fun _fromFile(filePath: Path, handler: FileFormatHandler): GameModel {
        val inputStream = Files.newInputStream(filePath, StandardOpenOption.READ)
        return handler.doImport(inputStream)
    }

    private fun getRecognizedFormats(formatEvaluator: Function<FileFormat, Boolean>): Set<FileFormat> {
        return Arrays.stream(FileFormat.values())
                .filter { t: FileFormat -> formatEvaluator.apply(t) }
                .collect(Collectors.toSet())
    }
}