package codes.nibby.yi.go

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class ZobristHasherTest {

    @Test
    fun `identical hash for empty board position`() {
        val gameModel = GoGameModel(3, 3, GoGameRules.CHINESE)
        val zobrist = ZobristHasher(gameModel.boardWidth, gameModel.boardHeight)
        val gamePosition = GoGamePosition(3, 3)
        val currentNode = gameModel.currentNode
        val state = GoGameState(gameModel, gamePosition, currentNode, 0, 0, 0)

        val firstHash = zobrist.calculateStateHash(state)
        val secondHash = zobrist.calculateStateHash(state)

        Assertions.assertEquals(firstHash, secondHash)
    }

    @Test
    fun `identical hash for some arbitrary board position`() {
        val gameModel = GoGameModel(3, 3, GoGameRules.CHINESE)
        val currentNode = gameModel.currentNode
        val zobrist = ZobristHasher(gameModel.boardWidth, gameModel.boardHeight)
        val gamePosition = GoGamePosition(3, 3)

        // Randomize the game position
        val randomSeed = 1234L
        val random = Random(randomSeed)

        for (iteration in 0 .. 10) {
            for (position in 0 until gameModel.getIntersectionCount()) {
                val number = random.nextInt(GoStoneColor.values().size)
                val color: GoStoneColor

                color = when (number) {
                    1 -> GoStoneColor.BLACK
                    2 -> GoStoneColor.WHITE
                    else -> GoStoneColor.NONE
                }

                // Manually manipulate the intersection state,
                gamePosition.intersectionState[position] = color
            }
        }

        val state = GoGameState(gameModel, gamePosition, currentNode, 0, 0, 0)

        // Hash twice
        val firstHash = zobrist.calculateStateHash(state)
        val secondHash = zobrist.calculateStateHash(state)

        Assertions.assertEquals(firstHash, secondHash)
    }

}