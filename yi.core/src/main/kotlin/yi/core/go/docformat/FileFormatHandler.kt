package yi.core.go.docformat

import yi.core.go.GameModel
import java.io.BufferedReader
import java.io.BufferedWriter

/**
 * Specifies the contract for a supported file format that a [yi.core.go.GameModel]
 * can be saved and parsed from.
 */
interface FileFormatHandler {

    /**
     *
     * @return A human readable, non-acronym name for this file format.
     */
    fun getUserFriendlyName(): String

    /**
     *
     * @return A name that is of shorter form than [getUserFriendlyName], allowing acronyms.
     */
    fun getAbbreviatedName(): String

    /**
     *
     * @return Set of all filename extensions considered to be of this file format, case insensitive.
     */
    fun getSupportedFileNameExtensions(): Set<String>

    /**
     * Quickly scans a file to see if its contents conform to the specifications laid out by
     * this file format.
     * <p/>
     * This method will not guarantee that the file is perfectly legal. Rather, it is an
     * indication that the file is likely legal.
     *
     * @return true if the file is likely of this format.
     */
    fun isLikelyLoadable(reader: BufferedReader): Boolean

    /**
     * Attempt to parse the specified file into a [GameModel], assuming the file is of this format.
     *
     * @apiNote Each format handler is a singleton accessible from [FileFormat],
     * therefore operations must be designed to be stateless.
     */
    fun doImport(reader: BufferedReader): GameModel

    /**
     * Saves the game model to the destination file, replacing the original file if it exists.
     *
     * This operation assumes that the write destination is available and has
     * write access.
     *
     * @apiNote Each format handler is a singleton accessible from [FileFormat],
     * therefore operations must be designed to be stateless.
     */
    fun doExport(gameModel: GameModel, writer: BufferedWriter)
}