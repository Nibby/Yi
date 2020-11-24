package yi.core.go

import yi.core.go.docformat.FileFormat
import java.nio.file.Files
import java.nio.file.Path

object GameModelExporter {

    fun toFile(gameModel: GameModel, destination: Path, format: FileFormat) {
        val outputStream = Files.newOutputStream(destination)
        val writer = outputStream.writer(Charsets.UTF_8);
        writer.use {
            format.getHandler().doExport(gameModel, writer)
        }
    }

}
