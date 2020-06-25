package codes.nibby.yi.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameNodeTest {

    @Test
    fun testSetParent_NodeMarkedAsRoot_Fails() {
        val node = GameNode()
        node.markAsRoot()
        val dummyParent = GameNode()

        assertThrows<IllegalStateException> { node.setParent(dummyParent) }
    }


}