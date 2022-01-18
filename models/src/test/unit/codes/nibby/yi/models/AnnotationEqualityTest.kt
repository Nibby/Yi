package codes.nibby.yi.models

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import codes.nibby.yi.models.Annotation.*

class AnnotationEqualityTest {

    @Test
    fun `point annotations with different type but same location are not equal`() {
        val triangle = Triangle(0, 0)
        val square = Square(0, 0)

        Assertions.assertNotEquals(triangle, square)
    }

    @Test
    fun `point annotations with same type and same location are equal`() {
        val triangle = Triangle(0, 0)
        val anotherTriangle = Triangle(0, 0)

        Assertions.assertEquals(triangle, anotherTriangle)
    }

    @Test
    fun `directional annotations with same type and same vector are equal`() {
        val line = Line(0, 0, 1, 1)
        val anotherLine = Line(0, 0, 1, 1)

        Assertions.assertEquals(line, anotherLine)
    }

    @Test
    fun `directional annotations with different type and same vector are not equal`() {
        val arrow = Arrow(0, 0, 1, 1)
        val line = Line(0, 0, 1, 1)

        Assertions.assertNotEquals(arrow, line)
    }

    @Test
    fun `directional annotations with same type but inverted direction are not equal`() {
        val line = Arrow(0, 0, 1, 1)
        val anotherLine = Line(1, 1, 0, 0)

        Assertions.assertNotEquals(line, anotherLine)
    }

}