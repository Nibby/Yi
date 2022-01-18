package codes.nibby.yi.models

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.HashSet

class ZobristHasherTest {

    @Test
    fun `identical hash for empty board position`() {
        val boardWidth = 3;
        val boardHeight = 3;
        val gameModel = GameModel(boardWidth, boardHeight, StandardGameRules.CHINESE)
        val zobrist = ZobristHasher(gameModel.boardWidth, gameModel.boardHeight)
        val gamePosition = GamePosition(boardWidth, boardHeight)
        val currentNode = gameModel.currentNode
        val state = GameState(gameModel, gamePosition, currentNode, 0, 0, HashSet())

        val firstHash = zobrist.computeStateHash(state, boardWidth, boardHeight)
        val secondHash = zobrist.computeStateHash(state, boardWidth, boardHeight)

        Assertions.assertEquals(firstHash, secondHash)
    }

    @Test
    fun `identical hash for some arbitrary board position`() {
        val boardWidth = 3;
        val boardHeight = 3;
        val gameModel = GameModel(boardWidth, boardHeight, StandardGameRules.CHINESE)
        val currentNode = gameModel.currentNode
        val zobrist = ZobristHasher(gameModel.boardWidth, gameModel.boardHeight)
        val gamePosition = GamePosition(boardWidth, boardHeight)

        // Randomize the game position
        val randomSeed = 1234L
        val random = Random(randomSeed)

        for (iteration in 0 .. 10) {
            for (position in 0 until gameModel.getIntersectionCount()) {
                val number = random.nextInt(StoneColor.values().size)
                val color: StoneColor

                color = when (number) {
                    1 -> StoneColor.BLACK
                    2 -> StoneColor.WHITE
                    else -> StoneColor.NONE
                }

                // Manually manipulate the intersection state,
                gamePosition.intersectionState[position] = color
            }
        }

        val state = GameState(gameModel, gamePosition, currentNode, 0, 0, HashSet())

        // Hash twice
        val firstHash = zobrist.computeStateHash(state, boardWidth, boardHeight)
        val secondHash = zobrist.computeStateHash(state, boardWidth, boardHeight)

        Assertions.assertEquals(firstHash, secondHash)
    }

}