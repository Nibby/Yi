package yi.core.go

import kotlin.random.Random

/**
 * A simple XOR state hasher for [GameState].
 * <p/>
 * See https://en.wikipedia.org/wiki/Zobrist_hashing for more information.
 */
class ZobristHasher constructor(private val boardWidth: Int, boardHeight: Int) : GameStateHasher {

    private val intersectionCount = boardWidth * boardHeight
    private var hashLookup = Array<Long>(intersectionCount * StoneColor.values().size) { 0 }

    init {
        val usedNumbers: HashSet<Long> = HashSet()

        for (state in StoneColor.values().indices) {
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

                val hashStateIndex = state.toByte() * intersectionCount + index
                hashLookup[hashStateIndex] = uniqueStateHash
                usedNumbers.add(uniqueStateHash)
            }
        }
    }

    override fun computeStateHash(state: GameState, boardWidth: Int, boardHeight: Int): Long {
        val position = state.boardPosition
        var hash = computeEmptyPositionHash(boardWidth, boardHeight)

        position.intersectionState.forEachIndexed { stonePosition, stoneColor ->
            val intersectionHash = getHashValue(stoneColor, stonePosition)
            hash = hash xor intersectionHash
        }

        return hash
    }

    override fun computeUpdateHash(lastStateHash: Long, stoneUpdates: Set<Stone>): Long {
        var newHash = lastStateHash

        stoneUpdates.forEach { update ->
            val updateHash = getHashValue(update)
            newHash = newHash xor updateHash
        }

        return newHash
    }

    private fun getHashValue(data: Stone): Long {
        return getHashValue(data.stoneColor, data.x + data.y * boardWidth)
    }

    private fun getHashValue(color: StoneColor, position: Int): Long {
        return hashLookup[color.index * intersectionCount + position]
    }
}