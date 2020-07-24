package yi.core.go

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.TestGameRules.TestingGameRulesNoSuicide

class GoGameModelTest {

    @Test
    fun `model tree state is initialized correctly`() {
        val model = GoGameModel(19, 19, TestingGameRulesNoSuicide())

        Assertions.assertEquals(model.gameTree.rootNode, model.getCurrentMove())
    }

}