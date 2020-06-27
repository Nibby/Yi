package codes.nibby.yi.weiqi

import java.lang.IllegalStateException
import kotlin.random.Random

class ZobristHasher constructor(private val intersectionCount: Int) : StateHasher {

    private var hashLookup = Array<Long>(intersectionCount * GoStoneColor.values().size) { 0 }

    init {
        val usedNumbers: HashSet<Long> = HashSet()

        for (state in 0 until GoStoneColor.values().size) {
            for (index in 0 until intersectionCount) {
                var uniqueStateHash = Random.nextLong()
                var retries = 0

                while (usedNumbers.contains(uniqueStateHash)) {
                    uniqueStateHash = Random.nextLong()
                    ++retries

                    // Don't expect this to happen at all, but it's part of the core code and
                    // we don't want any endless loops if something goes wrong.
                    if (retries > 10)
                        throw IllegalStateException("Too many collisions generating unique state hash")
                }

                val hashStateIndex = getHashTableIndex(state.toByte(), index)
                hashLookup[hashStateIndex] = uniqueStateHash
                usedNumbers.add(uniqueStateHash)
            }
        }
    }

    override fun calculateStateHash(state: GoGameState): Long {
        val position = state.gamePosition
        var hash = 0L

        position.intersectionState.forEachIndexed { stonePosition, stoneColor ->
            val intersectionHash = hashLookup[getHashTableIndex(stoneColor.index, stonePosition)]
            hash = hash xor intersectionHash
        }

        return hash
    }

    private fun getHashTableIndex(state: Byte, position: Int): Int {
        return state * intersectionCount + position
    }
}