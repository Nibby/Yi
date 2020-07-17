package yi.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.GoGameModel
import yi.core.go.GoGameRules

class GoGameModelTest {

    @Test
    fun `model tree state is initialized correctly`() {
        val model = GoGameModel(19, 19, GoGameRules.CHINESE)

        Assertions.assertEquals(model.gameTree.rootNode, model.getCurrentMove())
    }

}