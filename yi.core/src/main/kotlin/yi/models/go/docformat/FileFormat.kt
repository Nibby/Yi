package yi.models.go.docformat

import yi.models.go.GameModel
import java.util.*

/**
 * List of supported importable and exportable data formats for [GameModel].
 */
enum class FileFormat {

    SGF {
        private val handler = SgfFileFormatHandler()
        override fun getHandler(): FileFormatHandler = handler
    };

    abstract fun getHandler(): FileFormatHandler

    /**
     *
     * @param fileExtension File extension to test, without leading period character.
     * @return true if the file format denoted by this extension is supported.
     */
    fun isSupportedFormat(fileExtension: String): Boolean {
        val realExtension = getRealExtension(fileExtension)

        return Arrays.stream(values())
                .map { enumValue -> enumValue.getHandler() }
                .anyMatch { handler -> handler.getSupportedFileNameExtensions().contains(realExtension) }
    }

    /**
     *
     * @param fileExtension File extension to find handler for.
     * @return A corresponding [FileFormatHandler] to work with the given file extension, or
     * [Optional.empty] if the format is not supported.
     */
    fun getHandlerForExtension(fileExtension: String): Optional<FileFormatHandler> {
        val realExtension = getRealExtension(fileExtension)

        return Arrays.stream(values())
                .map { enumValue -> enumValue.getHandler() }
                .filter { format: FileFormatHandler -> format.getSupportedFileNameExtensions().contains(realExtension) }
                .findAny()
    }

    /**
     *
     * @return Set of all supported file extensions.
     */
    fun getSupportedFileExtensions(): Set<String> {
        return getHandler().getSupportedFileNameExtensions()
    }

    private fun getRealExtension(fileExtension: String): String {
        return if (fileExtension.startsWith(".")) fileExtension.substring(1) else fileExtension
    }
}