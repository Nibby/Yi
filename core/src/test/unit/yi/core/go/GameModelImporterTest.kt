package yi.core.go

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import yi.core.go.docformat.FileFormat

class GameModelImporterTest {

    @Test
    fun `import linear sgf, works`() {
        val expectedMovePositions = HashMap<Int, Array<Int>>()
        expectedMovePositions[1] = arrayOf(3, 3)
        expectedMovePositions[2] = arrayOf(15, 3)
        expectedMovePositions[3] = arrayOf(3, 15)
        expectedMovePositions[4] = arrayOf(15, 16)
        expectedMovePositions[5] = arrayOf(15, 14)
        expectedMovePositions[6] = arrayOf(16, 14)
        expectedMovePositions[7] = arrayOf(16, 13)
        expectedMovePositions[8] = arrayOf(16, 15)
        expectedMovePositions[9] = arrayOf(15, 13)
        expectedMovePositions[10] = arrayOf(13, 16)
        expectedMovePositions[11] = arrayOf(15, 9)

        // method under test
        val gameModel = GameModelImporter.fromInternalResources("/sgf/linear.sgf", FileFormat.SGF, GameModelImporterTest::class.java)

        // assertions
        assertEquals(19, gameModel.boardWidth)
        assertEquals(19, gameModel.boardHeight)

        val root = gameModel.getRootNode()
        // traverse down main variation
        var currentNode = root
        var moveNumber = 0

        while (currentNode.getNextNodeInMainBranch() != null) {
            currentNode = currentNode.getNextNodeInMainBranch()!!
            ++moveNumber

            assertEquals(GameNodeType.MOVE_PLAYED, currentNode.getType(), "Each node in SGF is a primary move, but this node is parsed as something else.")

            val gameMove = currentNode.getPrimaryMove()!!
            val expectedMoveColor = if ((moveNumber) % 2 == 1) StoneColor.BLACK else StoneColor.WHITE
            assertEquals(expectedMoveColor, gameMove.color, "Stone color mismatch at move ${currentNode.getMoveHistory().size}")

            val expectedPosition = expectedMovePositions[moveNumber]
            assertArrayEquals(expectedPosition, arrayOf(gameMove.x, gameMove.y), "Stone position mismatch at move $moveNumber")

            if (!currentNode.isLastMoveInThisVariation()) {
                assertTrue(currentNode.children.size == 1, "Linear SGF should only have 1 descendant at each node besides leaf")
            } else {
                assertTrue(currentNode.children.size == 0, "Last child must not have a child")
            }
        }

        assertEquals(12, currentNode.getMoveHistory().size, "Move history size mismatch")
    }

    @Test
    fun `import branched sgf, works`() {

    }

    @Test
    fun `import OGS-style SGF, works`() {

    }

    @Test
    fun `import SGF with stone edit, works`() {

    }

    @Test
    fun `import SGF with two passes, works`() {

    }

    @Test
    fun `import SGF with comment containing escaped delimiter symbols, parses comment and game file correctly`() {

    }

    @Test
    fun `import big SGF, works`() {

    }

    @Test
    fun `import corrupt SGF with tag value closure, ignores the tag and succeeds`() {

    }

    @Test
    fun `import SGF with random whitespace between key delimiter, works`() {

    }

    @Test
    fun `import corrupt SGF with missing branch closure, fails`() {

    }
}