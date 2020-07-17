package yi.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.*
import java.util.*
import kotlin.collections.HashSet

class ZobristHasherTest {

    @Test
    fun `identical hash for empty board position`() {
        val boardWidth = 3;
        val boardHeight = 3;
        val gameModel = GoGameModel(boardWidth, boardHeight, GoGameRules.CHINESE)
        val zobrist = GoZobristHasher(gameModel.boardWidth, gameModel.boardHeight)
        val gamePosition = GoGamePosition(boardWidth, boardHeight)
        val currentNode = gameModel.getCurrentMove()
        val state = GoGameState(gameModel, gamePosition, currentNode, 0, 0, HashSet(), 0)

        val firstHash = zobrist.computeStateHash(state, boardWidth, boardHeight)
        val secondHash = zobrist.computeStateHash(state, boardWidth, boardHeight)

        Assertions.assertEquals(firstHash, secondHash)
    }

    @Test
    fun `identical hash for some arbitrary board position`() {
        val boardWidth = 3;
        val boardHeight = 3;
        val gameModel = GoGameModel(boardWidth, boardHeight, GoGameRules.CHINESE)
        val currentNode = gameModel.getCurrentMove()
        val zobrist = GoZobristHasher(gameModel.boardWidth, gameModel.boardHeight)
        val gamePosition = GoGamePosition(boardWidth, boardHeight)

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

        val state = GoGameState(gameModel, gamePosition, currentNode, 0, 0, HashSet(), 0)

        // Hash twice
        val firstHash = zobrist.computeStateHash(state, boardWidth, boardHeight)
        val secondHash = zobrist.computeStateHash(state, boardWidth, boardHeight)

        Assertions.assertEquals(firstHash, secondHash)
    }

}