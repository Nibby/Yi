package yi.core.go.docformat

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.Annotation
import yi.core.go.GameModelImporter
import yi.core.go.GameNodeType
import yi.core.go.StoneColor
import java.util.concurrent.TimeUnit

class SgfFileFormatHandlerTest {

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
        val gameModel = GameModelImporter.fromInternalResources("/sgf/linear.sgf", FileFormat.SGF, this::class.java)

        // assertions
        Assertions.assertEquals(19, gameModel.boardWidth)
        Assertions.assertEquals(19, gameModel.boardHeight)

        val root = gameModel.getRootNode()
        // traverse down main variation
        var currentNode = root
        var moveNumber = 0

        while (currentNode.getNextNodeInMainBranch() != null) {
            currentNode = currentNode.getNextNodeInMainBranch()!!
            ++moveNumber

            Assertions.assertEquals(GameNodeType.MOVE_PLAYED, currentNode.getType(), "Each node in SGF is a primary move, but this node is parsed as something else.")

            val gameMove = currentNode.getPrimaryMove()!!
            val expectedMoveColor = if ((moveNumber) % 2 == 1) StoneColor.BLACK else StoneColor.WHITE
            Assertions.assertEquals(expectedMoveColor, gameMove.color, "Stone color mismatch at move ${currentNode.getMoveHistory().size}")

            val expectedPosition = expectedMovePositions[moveNumber]
            Assertions.assertArrayEquals(expectedPosition, arrayOf(gameMove.x, gameMove.y), "Stone position mismatch at move $moveNumber")

            if (!currentNode.isLastMoveInThisVariation()) {
                Assertions.assertTrue(currentNode.children.size == 1, "Linear SGF should only have 1 descendant at each node besides leaf")
            } else {
                Assertions.assertTrue(currentNode.children.size == 0, "Last child must not have a child")
            }
        }

        Assertions.assertEquals(12, currentNode.getMoveHistory().size, "Move history size mismatch")
    }

    @Test
    fun `import branched sgf, works`() {
        // Method under test
        val gameModel = GameModelImporter.fromInternalResources("/sgf/branches.sgf", FileFormat.SGF, this::class.java)

        // Assertions
        Assertions.assertEquals(19, gameModel.boardWidth)
        Assertions.assertEquals(19, gameModel.boardHeight)

        val root = gameModel.getRootNode()
        Assertions.assertEquals(3, root.children.size)

        val rootChild1 = root.children[0]
        Assertions.assertTrue(rootChild1.isLastMoveInThisVariation())

        val rootChild3 = root.children[2]
        Assertions.assertTrue(rootChild3.isLastMoveInThisVariation())

        val rootChild2 = root.children[1]
        Assertions.assertEquals(1, rootChild2.children.size)

        val rootChild2a = rootChild2.children[0]
        Assertions.assertEquals(2, rootChild2a.children.size)

        val rootChild2b = rootChild2a.children[0]
        Assertions.assertEquals(1, rootChild2b.children.size)

        val rootChild2c = rootChild2b.children[0]
        Assertions.assertTrue(rootChild2c.isLastMoveInThisVariation())
    }

    @Test
    fun `import OGS-style SGF, works`() {
        /*
            OGS has a very peculiar format where a standard continuation is expressed as a
            new branch of size 1. So instead of:
                ;B[aa] ;W[ab] ;B[ac] ...
            We have:
                ;B[aa] (;W[ab] (;B[ac] (...)))
         */
        // Method under test
        val gameModel = GameModelImporter.fromInternalResources("/sgf/ogsLinear.sgf", FileFormat.SGF, this::class.java)

        // Assertions
        Assertions.assertEquals(19, gameModel.boardWidth)
        Assertions.assertEquals(19, gameModel.boardHeight)

        val root = gameModel.getRootNode()
        // traverse down main variation
        var currentNode = root
        var moveNumber = 0
        while (!currentNode.isLastMoveInThisVariation()) {
            currentNode = currentNode.getNextNodeInMainBranch()!!
            ++moveNumber
        }

        Assertions.assertEquals(188, moveNumber)
    }

    @Test
    fun `import SGF with stone edit, works`() {
        // Method under test
        val gameModel = GameModelImporter.fromInternalResources("/sgf/blink.sgf", FileFormat.SGF, this::class.java)

        // Assertions
        Assertions.assertEquals(19, gameModel.boardWidth)
        Assertions.assertEquals(19, gameModel.boardHeight)

        val root = gameModel.getRootNode()

        val firstMove = root.getNextNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.STONE_EDIT, firstMove.getType())
        Assertions.assertEquals(StoneColor.WHITE, gameModel.getGameState(firstMove).boardPosition.getStoneColorAt(1, 1))

        val secondMove = firstMove.getNextNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.STONE_EDIT, secondMove.getType())
        Assertions.assertEquals(StoneColor.NONE, gameModel.getGameState(secondMove).boardPosition.getStoneColorAt(1, 1))

        val thirdMove = secondMove.getNextNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.STONE_EDIT, thirdMove.getType())
        Assertions.assertEquals(StoneColor.WHITE, gameModel.getGameState(thirdMove).boardPosition.getStoneColorAt(1, 1))
    }

    @Test
    fun `import SGF with two passes, works`() {
        // Method under test
        val gameModel = GameModelImporter.fromInternalResources("/sgf/twoPasses.sgf", FileFormat.SGF, this::class.java)

        // Assertions
        Assertions.assertEquals(19, gameModel.boardWidth)
        Assertions.assertEquals(19, gameModel.boardHeight)

        val root = gameModel.getRootNode()

        val firstMove = root.getNextNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.PASS, firstMove.getType())

        val secondMove = firstMove.getNextNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.PASS, secondMove.getType())

        Assertions.assertTrue(secondMove.isLastMoveInThisVariation())
    }

    @Test
    fun `import 1000 node SGF, is fast enough`() {
        val startTime = System.nanoTime()
        GameModelImporter.fromInternalResources("/sgf/1000_nodes.sgf", FileFormat.SGF, this::class.java)
        val finishTime = System.nanoTime()

        val maxDurationMs = 500L
        Assertions.assertTrue(finishTime - startTime < TimeUnit.MILLISECONDS.toNanos(maxDurationMs), "Too slow. Expected < $maxDurationMs")
    }

    @Test
    fun `import SGF with comment containing escaped delimiter symbols, parses comment and game file correctly`() {
        // TODO: Implement this in a new commit.
//        // Method under test
//        val gameModel = GameModelImporter.fromInternalResources("/sgf/escapedSymbols.sgf", FileFormat.SGF, this::class.java)
//
//        val firstNode = gameModel.getRootNode().getNextNodeInMainBranch()!!
//        Assertions.assertEquals("This comment has an escaped tag close ] character and meta-escaped characters \\]", firstNode.getComments())
//
//        val secondNode = firstNode.getNextNodeInMainBranch()!!
//        Assertions.assertTrue(secondNode.isLastMoveInThisVariation())
    }

    @Test
    fun `import corrupt SGF with tag value closure, ignores the tag and succeeds`() {
        val gameModel = GameModelImporter.fromInternalResources("/sgf/corruptTagData.sgf", FileFormat.SGF, this::class.java)
        val root = gameModel.getRootNode()

        val firstMove = root.getNextNodeInMainBranch()
        Assertions.assertNotNull(firstMove, "Unexpected end of sequence after root node")

        val secondMove = firstMove!!.getNextNodeInMainBranch()
        Assertions.assertNotNull(secondMove, "Unexpected end of sequence after first move")

        // Should expect no annotations because both annotation tags are conjoined, forming
        // one inconsistent value.
        Assertions.assertEquals(0, firstMove.getAnnotationsOriginal().size)
    }

    @Test
    fun `import SGF with random whitespace between key delimiter, works`() {
        val gameModel = GameModelImporter.fromInternalResources("/sgf/spaceInTagData.sgf", FileFormat.SGF, this::class.java)

        val root = gameModel.getRootNode()
        Assertions.assertEquals("6.5", root.getMetadataSingleValue("KM"), "Komi value incorrect")
        Assertions.assertEquals(19, gameModel.boardWidth, "Board width size incorrect")
        Assertions.assertEquals(19, gameModel.boardHeight, "Board height size incorrect")
        Assertions.assertEquals("2020-08-23", root.getMetadataSingleValue("DT"), "Date value incorrect")

        val firstMove = root.getNextNodeInMainBranch()!!
        val annotations = firstMove.getAnnotationsOriginal()
        Assertions.assertEquals(2, annotations.size)
        Assertions.assertTrue(annotations.contains(Annotation.Square(0, 0)))
        Assertions.assertTrue(annotations.contains(Annotation.Square(0, 1)))
        Assertions.assertEquals("test msg", firstMove.getComments())
    }

    @Test
    fun `import corrupt SGF with missing branch closure, ignores branch`() {
        TODO("Implement this")
    }

    @Test
    fun `essential SGF tags well covered by importer`() {
        // Check that the root node and game node are constructed correctly from
        // key SGF tags such as B, W, C, AB, AW etc.
        TODO("Implement this")
    }
}