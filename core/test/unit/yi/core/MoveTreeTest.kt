package yi.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.common.GameNode
import yi.core.common.GameTree
import java.lang.IllegalArgumentException

/**
 * Test the tree hierarchy of [GameNode] is managed correctly by [GameTree]
 */
class MoveTreeTest {

    /* Filler node data used for testing purposes */
    private class Data

    @Test
    fun `node distance to root calculation is correct`() {
        val tree = GameTree<Data>()
        val nodeA = GameNode<Data>()
        tree.appendNode(tree.rootNode, nodeA)

        Assertions.assertEquals(0, tree.rootNode.getDistanceToRoot())
        Assertions.assertEquals(1, nodeA.getDistanceToRoot())
    }

    @Test
    fun `linear node path to root is correct`() {
        val tree = GameTree<Data>()
        val steps = listOf(GameNode<Data>(), GameNode(), GameNode(), GameNode(), GameNode())

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
        val path = lastNode.getPathToRoot()
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
        val tree = GameTree<Data>()

        Assertions.assertTrue(tree.rootNode.isRoot())
    }

    @Test
    fun `appendNode() sets correct children on parent node`() {
        val tree = GameTree<Data>()
        val firstChild = GameNode<Data>()

        tree.appendNode(tree.rootNode, firstChild)

        Assertions.assertEquals(1, tree.rootNode.children.size)
    }

    @Test
    fun `appendNode() sets correct parent on child node`() {
        val tree = GameTree<Data>()
        val firstChild = GameNode<Data>()

        tree.appendNode(tree.rootNode, firstChild)
        Assertions.assertEquals(tree.rootNode, firstChild.parent)
    }

    @Test
    fun `appendNode() multiple child on one parent sets correct child order`() {
        val tree = GameTree<Data>()
        val firstChild = GameNode<Data>()
        val secondChild = GameNode<Data>()

        tree.appendNode(tree.rootNode, firstChild)
        tree.appendNode(tree.rootNode, secondChild)

        // Check that the children are added in insert order
        // that is, the second child is at index 1.
        Assertions.assertEquals(1, tree.rootNode.children.indexOf(secondChild))
    }

    @Test
    fun `removeNode() on leaf correctly sets parent node state`() {
        val tree = GameTree<Data>()
        val leaf = GameNode<Data>()
        tree.appendNode(tree.rootNode, leaf)

        tree.removeNode(leaf)

        Assertions.assertEquals(0, tree.rootNode.children.size)
    }

    @Test
    fun `removeNode() breaks tree hierarchy and split tree into two`() {
        val tree = GameTree<Data>()
        val level1 = GameNode<Data>()
        val level2 = GameNode<Data>()
        val level3 = GameNode<Data>()
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
        val tree = GameTree<Data>()
        val level1 = GameNode<Data>()
        val level2 = GameNode<Data>()
        val level3 = GameNode<Data>()
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
        val treeA = GameTree<Data>()
        val nodeA = GameNode<Data>()
        treeA.appendNode(treeA.rootNode, nodeA)

        val treeB = GameTree<Data>()

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            treeB.removeNode(nodeA)
        }
    }

    @Test
    fun `adding node to treeB when it is already in treeA should fail`() {
        val treeA = GameTree<Data>()
        val nodeA = GameNode<Data>()
        treeA.appendNode(nodeA)

        val treeB = GameTree<Data>()

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            treeB.appendNode(nodeA)
        }
    }

    @Test
    fun `appending root node to a parent should fail`() {
        val treeA = GameTree<Data>()
        val treeB = GameTree<Data>()

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            treeA.appendNode(treeB.rootNode)
        }
    }

    @Test
    fun `appending same node as child should fail`() {
        val treeA = GameTree<Data>()

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            treeA.appendNode(treeA.rootNode)
        }
    }

    @Test
    fun `root node has correct position`() {
        val treeA = GameTree<Data>()

        Assertions.assertEquals(0, treeA.rootNode.position)
        Assertions.assertEquals(0, treeA.rootNode.getDistanceToRoot())
    }

    @Test
    fun `child node has correct position`() {
        val treeA = GameTree<Data>()
        val child1 = GameNode<Data>()
        treeA.appendNode(child1)

        Assertions.assertEquals(1, child1.position)
        Assertions.assertEquals(1, child1.getDistanceToRoot())
    }
}