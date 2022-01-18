package codes.nibby.yi.models

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Test the tree hierarchy of [GameNode] is managed correctly by [GameTree]
 */
class MoveTreeTest {

    private fun node(): GameNode {
        return GameNode(StateDelta.forPassMove(0))
    }

    @Test
    fun `node distance to root calculation is correct`() {
        val tree = GameTree(0)
        val nodeA = node()
        tree.appendNode(tree.rootNode, nodeA)

        Assertions.assertEquals(0, tree.rootNode.moveNumber)
        Assertions.assertEquals(1, nodeA.moveNumber)
    }

    @Test
    fun `linear node path to root is correct`() {
        val tree = GameTree(0)
        val steps = listOf(node(), node(), node(), node(), node())

        // Establish linked hierarchy
        for (step in 0..steps.size) {
            val parent = if (step == 0) tree.rootNode else steps[step-1]
            val child = if (step != steps.size) steps[step] else null

            if (child != null) {
                tree.appendNode(parent, child)
            }
        }

        // Check path to root
        val lastNode = steps[steps.lastIndex]
        val path = lastNode.getMoveHistory()
        var nextCorrectNode = tree.rootNode
        for (step in 0..steps.size) {
            val pathNode = path[step]
            Assertions.assertEquals(nextCorrectNode, pathNode)

            if (step != steps.size) {
                nextCorrectNode = nextCorrectNode.children[0]
            }
        }
    }

    @Test
    fun `tree has correct root node state`() {
        val tree = GameTree(0)

        Assertions.assertTrue(tree.rootNode.isRoot())
    }

    @Test
    fun `appendNode() sets correct children on parent node`() {
        val tree = GameTree(0)
        val firstChild = node()

        tree.appendNode(tree.rootNode, firstChild)

        Assertions.assertEquals(1, tree.rootNode.children.size)
    }

    @Test
    fun `appendNode() sets correct parent on child node`() {
        val tree = GameTree(0)
        val firstChild = node()

        tree.appendNode(tree.rootNode, firstChild)
        Assertions.assertEquals(tree.rootNode, firstChild.parent)
    }

    @Test
    fun `appendNode() multiple child on one parent sets correct child order`() {
        val tree = GameTree(0)
        val firstChild = node()
        val secondChild = node()

        tree.appendNode(tree.rootNode, firstChild)
        tree.appendNode(tree.rootNode, secondChild)

        // Check that the children are added in insert order
        // that is, the second child is at index 1.
        Assertions.assertEquals(1, tree.rootNode.children.indexOf(secondChild))
    }

    @Test
    fun `removeNode() on leaf correctly sets parent node state`() {
        val tree = GameTree(0)
        val leaf = node()
        tree.appendNode(tree.rootNode, leaf)

        tree.removeNode(leaf)

        Assertions.assertEquals(0, tree.rootNode.children.size)
    }

    @Test
    fun `removeNode() breaks tree hierarchy and split tree into two`() {
        val tree = GameTree(0)
        val level1 = node()
        val level2 = node()
        val level3 = node()
        tree.appendNode(tree.rootNode, level1)
        tree.appendNode(level1, level2)
        tree.appendNode(level2, level3)

        tree.removeNode(level1)

        Assertions.assertEquals(0, tree.rootNode.children.size)
        Assertions.assertNull(level2.parent)
        Assertions.assertEquals(1, level2.children.size)
    }

    @Test
    fun `removeNodeSubtree() on internal node should make its parent leaf`() {
        val tree = GameTree(0)
        val level1 = node()
        val level2 = node()
        val level3 = node()
        tree.appendNode(tree.rootNode, level1)
        tree.appendNode(level1, level2)
        tree.appendNode(level2, level3)

        tree.removeNodeSubtree(level1)

        Assertions.assertEquals(0, tree.rootNode.children.size)
        Assertions.assertNull(level2.parent)
        Assertions.assertEquals(0, level2.children.size)
        Assertions.assertNull(level3.parent)
    }

    @Test
    fun `removing node on treeA from treeB should fail`() {
        val treeA = GameTree(0)
        val nodeA = node()
        treeA.appendNode(treeA.rootNode, nodeA)

        val treeB = GameTree(0)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            treeB.removeNode(nodeA)
        }
    }

    @Test
    fun `adding node to treeB when it is already in treeA should fail`() {
        val treeA = GameTree(0)
        val nodeA = node()
        treeA.appendNewNodeToRoot(nodeA)

        val treeB = GameTree(0)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            treeB.appendNewNodeToRoot(nodeA)
        }
    }

    @Test
    fun `appending root node to a parent should fail`() {
        val treeA = GameTree(0)
        val treeB = GameTree(0)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            treeA.appendNewNodeToRoot(treeB.rootNode)
        }
    }

    @Test
    fun `appending same node as child should fail`() {
        val treeA = GameTree(0)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            treeA.appendNewNodeToRoot(treeA.rootNode)
        }
    }

    @Test
    fun `root node has correct position`() {
        val treeA = GameTree(0)

        Assertions.assertEquals(0, treeA.rootNode.moveNumber)
        Assertions.assertEquals(0, treeA.rootNode.moveNumber)
    }

    @Test
    fun `child node has correct position`() {
        val treeA = GameTree(0)
        val child1 = node()
        treeA.appendNewNodeToRoot(child1)

        Assertions.assertEquals(1, child1.moveNumber)
        Assertions.assertEquals(1, child1.moveNumber)
    }
}