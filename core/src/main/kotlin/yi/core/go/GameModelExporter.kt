package yi.core.go

import yi.core.go.docformat.FileFormat
import java.nio.file.Files
import java.nio.file.Path

object GameModelExporter {

    fun toFile(gameModel: GameModel, destination: Path, format: FileFormat) {
        val outputStream = Files.newOutputStream(destination)
        outputStream.use {
            format.getHandler().doExport(gameModel, outputStream)
        }
    }

}
