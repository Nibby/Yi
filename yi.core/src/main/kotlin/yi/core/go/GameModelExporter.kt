package yi.core.go

import yi.core.go.GameModelExporter.toOutputStream
import yi.core.go.docformat.FileFormat
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * Provides the functionality to persist a [GameModel] from memory to another source
 * such as files on a hard disk. Alternatively, the model can be transformed into a
 * stream of bytes using [toOutputStream].
 *
 * To import a game model, use the complementary class [GameModelImporter].
 */
object GameModelExporter {

    /**
     * Saves a [GameModel] to a file under a specified format.
     *
     * @param gameModel Game model to be saved.
     * @param destination Path of the game record file to be written.
     * @param format Storage format of the game model data. See [FileFormat] for supported
     * values.
     */
    fun toFile(gameModel: GameModel, destination: Path, format: FileFormat) {
        val outputStream = Files.newOutputStream(destination)
        toOutputStream(gameModel, outputStream, format)
    }

    /**
     * Writes [GameModel] data to a designated [OutputStream] under a specified format.
     *
     * @param gameModel Game model to be written.
     * @param outputStream Stream to write game model data to.
     * @param format Storage format of the game model data. See [FileFormat] for supported
     * values.
     */
    fun toOutputStream(gameModel: GameModel, outputStream: OutputStream, format: FileFormat) {
        val writer = outputStream.bufferedWriter(Charsets.UTF_8)
        writer.use {
            format.getHandler().doExport(gameModel, writer)
        }
    }

}
