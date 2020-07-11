package codes.nibby.go

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GoGameModelTest {

    @Test
    fun `model tree state is initialized correctly`() {
        val model = GoGameModel(19, 19, GoGameRules.CHINESE)

        Assertions.assertEquals(model.moveTree.rootNode, model.currentNode)
    }

}