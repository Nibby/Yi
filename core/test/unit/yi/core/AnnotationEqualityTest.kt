package yi.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AnnotationEqualityTest {

    @Test
    fun `point annotations with different type but same location are not equal`() {
        val triangle = GoAnnotation.Triangle(0, 0)
        val square = GoAnnotation.Square(0, 0)

        Assertions.assertNotEquals(triangle, square)
    }

    @Test
    fun `point annotations with same type and same location are equal`() {
        val triangle = GoAnnotation.Triangle(0, 0)
        val anotherTriangle = GoAnnotation.Triangle(0, 0)

        Assertions.assertEquals(triangle, anotherTriangle)
    }

    @Test
    fun `directional annotations with same type and same vector are equal`() {
        val line = GoAnnotation.Line(0, 0, 1, 1)
        val anotherLine = GoAnnotation.Line(0, 0, 1, 1)

        Assertions.assertEquals(line, anotherLine)
    }

    @Test
    fun `directional annotations with different type and same vector are not equal`() {
        val arrow = GoAnnotation.Arrow(0, 0, 1, 1)
        val line = GoAnnotation.Line(0, 0, 1, 1)

        Assertions.assertNotEquals(arrow, line)
    }

    @Test
    fun `directional annotations with same type but inverted direction are not equal`() {
        val line = GoAnnotation.Arrow(0, 0, 1, 1)
        val anotherLine = GoAnnotation.Line(1, 1, 0, 0)

        Assertions.assertNotEquals(line, anotherLine)
    }

}