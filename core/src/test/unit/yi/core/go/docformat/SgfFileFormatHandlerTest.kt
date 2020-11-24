package yi.core.go.docformat

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.*
import yi.core.go.Annotation
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

        val maxDurationMs = 800L
        val duration = (finishTime - startTime)
        Assertions.assertTrue(duration < TimeUnit.MILLISECONDS.toNanos(maxDurationMs),
                "Too slow. Expected < $maxDurationMs, got $duration")
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
    fun `essential SGF tags well covered by importer`() {
        // Check that the root node and game node are constructed correctly from
        // key SGF tags such as B, W, C, AB, AW etc.
        val gameModel = GameModelImporter.fromInternalResources("/sgf/standard.sgf", FileFormat.SGF, this::class.java)

        val rootNode = gameModel.getRootNode()
        val annotations = rootNode.getAnnotationsCopy()

        Assertions.assertEquals(GameNodeType.ROOT, rootNode.delta.type)
        Assertions.assertEquals(13, annotations.size)
        Assertions.assertTrue(annotations.contains(Annotation.Square(0, 0)), "Square missing at (0, 0)")
        Assertions.assertTrue(annotations.contains(Annotation.Triangle(0, 1)), "Triangle missing at (0, 1)")
        Assertions.assertTrue(annotations.contains(Annotation.Cross(1, 0)), "Cross missing at (1, 0)")
        Assertions.assertTrue(annotations.contains(Annotation.Circle(1, 1)), "Circle missing at (1, 1)")
        Assertions.assertTrue(annotations.contains(Annotation.Label(2, 0, "1")), "Label '1' missing at (2, 0)")
        Assertions.assertTrue(annotations.contains(Annotation.Label(3, 0, "2")), "Label '2' missing at (3, 0)")
        Assertions.assertTrue(annotations.contains(Annotation.Label(4, 0, "3")), "Label '3' missing at (4, 0)")
        Assertions.assertTrue(annotations.contains(Annotation.Label(2, 1, "A")), "Label 'A' missing at (2, 1)")
        Assertions.assertTrue(annotations.contains(Annotation.Label(3, 1, "B")), "Label 'B' missing at (3, 1)")
        Assertions.assertTrue(annotations.contains(Annotation.Label(4, 1, "C")), "Label 'C' missing at (4, 1)")
        Assertions.assertTrue(annotations.contains(Annotation.Line(18, 0, 18, 3)), "Line missing (18, 0) -> (18, 3)")
        Assertions.assertTrue(annotations.contains(Annotation.Arrow(17, 0, 17, 3)), "Arrow missing (17, 0) -> (17, 3)")
        Assertions.assertTrue(annotations.contains(Annotation.Arrow(16, 3, 16, 0)), "Arrow missing (16, 3) -> (16, 0)")

        val move1 = rootNode.getNextNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.MOVE_PLAYED, move1.delta.type, "Move 1 node type incorrect")
        Assertions.assertEquals(Stone(3, 3, StoneColor.BLACK), move1.getPrimaryMove()!!, "First move stone position incorrect")
        Assertions.assertEquals(2, move1.getNextNodes().size, "First move continuation size incorrect")

        val move1a = move1.getNextNodesExcludingMainBranch()[0]
        Assertions.assertEquals(GameNodeType.STONE_EDIT, move1a.delta.type, "Move 1 variation node type incorrect")

        val move2 = move1.getNextNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.MOVE_PLAYED, move2.delta.type, "Move 2 node type incorrect")
        Assertions.assertEquals(Stone(15, 3, StoneColor.WHITE), move2.getPrimaryMove()!!, "Second move stone position incorrect")
        Assertions.assertEquals(2, move2.getNextNodes().size, "Second move continuation size incorrect")

        val move2a = move1.getNextNodesExcludingMainBranch()[0]
        Assertions.assertEquals(GameNodeType.STONE_EDIT, move2a.delta.type, "Move 2 variation node type incorrect")

        val move3 = move2.getNextNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.MOVE_PLAYED, move3.delta.type, "Move 3 node type incorrect")
        Assertions.assertEquals(Stone(3, 16, StoneColor.BLACK), move3.getPrimaryMove()!!, "Second move stone position incorrect")
        Assertions.assertEquals(1, move3.getNextNodes().size, "Third move continuation size incorrect")

        val move4 = move3.getNextNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.MOVE_PLAYED, move4.delta.type, "Move 4 node type incorrect")

        val move5 = move4.getNextNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.PASS, move5.delta.type, "Move 5 node type incorrect")
    }
}