package yi.models.go

import java.util.*
import java.util.function.BiConsumer
import kotlin.collections.HashSet

/**
 * Stores metadata information about a [GameModel] such as the player names, rank and
 * time settings. While most information stored are purely informative, some values do
 * service a functional purpose. See documentation on relevant setters for more information.
 */
class GameModelInfo {

    private companion object {
        const val KEY_KOMI = "komi"
        const val KEY_APPLICATION_NAME = "application"
        const val KEY_HANDICAP_COUNT = "handicap"
        const val KEY_PLAYER_BLACK_NAME = "playerBlackName"
        const val KEY_PLAYER_BLACK_RANK = "playerBlackRank"
        const val KEY_PLAYER_WHITE_NAME = "playerWhiteName"
        const val KEY_PLAYER_WHITE_RANK = "playerWhiteRank"
    }

    private val data = HashMap<String, Any>()
    private val changeListeners = HashSet<BiConsumer<String, Any>>()

    /**
     * Register a listener to be informed about any of the metadata value changes.
     */
    fun addChangeListener(listener: BiConsumer<String, Any>) {
        changeListeners.add(listener)
    }

    /**
     * Removes a previously registered listener for metadata value changes.
     */
    fun removeChangeListener(listener: BiConsumer<String, Any>) {
        changeListeners.remove(listener)
    }

    private fun put(key: String, value: Any) {
        data[key] = value
        changeListeners.forEach { it.accept(key, value) }
    }

    private fun <T> get(key: String): Optional<T> {
        val value: Any? = data[key]
        value?.let { return Optional.of(it as T) }
        return Optional.empty()
    }

    /**
     * @return Number of handicap stones used in this game. This value is used to determine
     * which player is to play the next move.
     */
    fun getHandicapCount(): Int {
        return get<Int>(KEY_HANDICAP_COUNT).orElse(0)
    }

    /**
     * Sets the number of handicap stones used in the game. This information
     * may be used to determine the player color on each turn.
     */
    fun setHandicapCount(handicaps: Int) {
        if (handicaps < 0) {
            throw IllegalArgumentException("Handicap count must be >= 0, got $handicaps")
        }
        put(KEY_HANDICAP_COUNT, handicaps)
    }

    /**
     * @return Name of the application that produced the data for this game model.
     */
    fun getApplicationName(): String {
        return get<String>(KEY_APPLICATION_NAME).orElse("")
    }

    /**
     * Sets the name of the application that last worked with this game record.
     */
    fun setApplicationName(value: String) {
        put(KEY_APPLICATION_NAME, value)
    }

    /**
     * @return The actual komi value used for this game, which is used for score
     * calculations. This is one of the few model metadata whose value serves a purpose.
     */
    fun getKomi(): Float {
        return get<Float>(KEY_KOMI).orElse(0f)
    }

    /**
     * Sets the value of komi used for the game. This information is used in score
     * calculations.
     */
    fun setKomi(value: Float) {
        put(KEY_KOMI, value)
    }

    /**
     * @return Name of the player who played black.
     */
    fun getPlayerBlackName(): String {
        return get<String>(KEY_PLAYER_BLACK_NAME).orElse("")
    }

    /**
     * Sets the name of the player who played black.
     */
    fun setPlayerBlackName(value: String) {
        put(KEY_PLAYER_BLACK_NAME, value)
    }

    /**
     * @return Rank of the player who played black.
     */
    fun getPlayerBlackRank(): String {
        return get<String>(KEY_PLAYER_BLACK_RANK).orElse("")
    }

    /**
     * Sets the rank of the player who played black.
     */
    fun setPlayerBlackRank(value: String) {
        put(KEY_PLAYER_BLACK_RANK, value)
    }

    /**
     * @return Name of the player who played white.
     */
    fun getPlayerWhiteName(): String {
        return get<String>(KEY_PLAYER_WHITE_NAME).orElse("")
    }

    /**
     * Sets the name of the player who played white.
     */
    fun setPlayerWhiteName(value: String) {
        put(KEY_PLAYER_WHITE_NAME, value)
    }

    /**
     * @return Rank data of the player who played white.
     */
    fun getPlayerWhiteRank(): String {
        return get<String>(KEY_PLAYER_WHITE_RANK).orElse("")
    }

    /**
     * Sets the rank of the player who played white.
     */
    fun setPlayerWhiteRank(value: String) {
        put(KEY_PLAYER_WHITE_RANK, value)
    }

    internal fun dispose() {
        data.clear()
        changeListeners.clear()
    }
}