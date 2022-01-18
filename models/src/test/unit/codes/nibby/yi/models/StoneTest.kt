package codes.nibby.yi.models

import codes.nibby.yi.models.Stone
import codes.nibby.yi.models.StoneColor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class StoneTest {

    @Test
    fun `identical stone color at the same position is equal`() {
        val data1 = Stone(0, 0, StoneColor.BLACK)
        val data2 = Stone(0, 0, StoneColor.BLACK)

        Assertions.assertTrue(data1 == data2)
        Assertions.assertTrue(data1.hashCode() == data2.hashCode())
    }

    @Test
    fun `different stone color at the same position does not equal`() {
        val data1 = Stone(0, 0, StoneColor.BLACK)
        val data2 = Stone(0, 0, StoneColor.WHITE)

        Assertions.assertFalse(data1 == data2)
        Assertions.assertFalse(data1.hashCode() == data2.hashCode())
    }

    @Test
    fun `different stone position despite same color is not equal`() {
        val data1 = Stone(1, 2, StoneColor.BLACK)
        val data2 = Stone(0, 1, StoneColor.BLACK)

        Assertions.assertFalse(data1 == data2)
        Assertions.assertFalse(data1.hashCode() == data2.hashCode())
    }
}