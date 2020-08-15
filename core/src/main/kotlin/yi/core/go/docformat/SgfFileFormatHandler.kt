package yi.core.go.docformat

import yi.core.go.GameModel
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

internal class SgfFileFormatHandler : FileFormatHandler {

    override fun getUserFriendlyName(): String {
        return "smart go file"
    }

    override fun getAbbreviatedName(): String {
        return "sgf"
    }

    override fun getSupportedFileNameExtensions(): Set<String> {
        return setOf("sgf")
    }

    override fun isLikelyLoadable(file: InputStream): Boolean {
        TODO("Not yet implemented")
    }

    override fun load(file: InputStream): GameModel {
        TODO("Not yet implemented")
    }

    override fun save(gameModel: GameModel, destination: OutputStream) {
        TODO("Not yet implemented")
    }
}