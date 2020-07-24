package yi.core.go

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.GoStoneColor
import yi.core.go.GoStoneData

class GoStoneDataTest {

    @Test
    fun `identical stone color at the same position is equal`() {
        val data1 = GoStoneData(0, 0, GoStoneColor.BLACK)
        val data2 = GoStoneData(0, 0, GoStoneColor.BLACK)

        Assertions.assertTrue(data1 == data2)
        Assertions.assertTrue(data1.hashCode() == data2.hashCode())
    }

    @Test
    fun `different stone color at the same position does not equal`() {
        val data1 = GoStoneData(0, 0, GoStoneColor.BLACK)
        val data2 = GoStoneData(0, 0, GoStoneColor.WHITE)

        Assertions.assertFalse(data1 == data2)
        Assertions.assertFalse(data1.hashCode() == data2.hashCode())
    }

    @Test
    fun `different stone position despite same color is not equal`() {
        val data1 = GoStoneData(1, 2, GoStoneColor.BLACK)
        val data2 = GoStoneData(0, 1, GoStoneColor.BLACK)

        Assertions.assertFalse(data1 == data2)
        Assertions.assertFalse(data1.hashCode() == data2.hashCode())
    }
}