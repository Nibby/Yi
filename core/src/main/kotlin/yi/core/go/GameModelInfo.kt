package yi.core.go

import java.util.*
import java.util.function.BiConsumer
import kotlin.collections.HashSet

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

    fun addChangeListener(listener: BiConsumer<String, Any>) {
        changeListeners.add(listener)
    }

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

    fun getHandicapCount(): Int {
        return get<Int>(KEY_HANDICAP_COUNT).orElse(0)
    }

    fun setHandicapCount(handicaps: Int) {
        if (handicaps < 0) {
            throw IllegalArgumentException("Handicap count must be >= 0, got $handicaps")
        }
        put(KEY_HANDICAP_COUNT, handicaps)
    }

    fun getApplicationName(): String {
        return get<String>(KEY_APPLICATION_NAME).orElse("")
    }

    fun setApplicationName(value: String) {
        put(KEY_APPLICATION_NAME, value)
    }

    fun getKomi(): Float {
        return get<Float>(KEY_KOMI).orElse(0f)
    }

    fun setKomi(value: Float) {
        put(KEY_KOMI, value)
    }

    fun getPlayerBlackName(): String {
        return get<String>(KEY_PLAYER_BLACK_NAME).orElse("")
    }

    fun setPlayerBlackName(value: String) {
        put(KEY_PLAYER_BLACK_NAME, value)
    }

    fun getPlayerBlackRank(): String {
        return get<String>(KEY_PLAYER_BLACK_RANK).orElse("")
    }

    fun setPlayerBlackRank(value: String) {
        put(KEY_PLAYER_BLACK_RANK, value)
    }

    fun getPlayerWhiteName(): String {
        return get<String>(KEY_PLAYER_WHITE_NAME).orElse("")
    }

    fun setPlayerWhiteName(value: String) {
        put(KEY_PLAYER_WHITE_NAME, value)
    }

    fun getPlayerWhiteRank(): String {
        return get<String>(KEY_PLAYER_WHITE_RANK).orElse("")
    }

    fun setPlayerWhiteRank(value: String) {
        put(KEY_PLAYER_WHITE_RANK, value)
    }

    internal fun dispose() {
        data.clear()
        changeListeners.clear()
    }
}