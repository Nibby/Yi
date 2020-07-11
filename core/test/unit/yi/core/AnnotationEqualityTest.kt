package yi.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AnnotationEqualityTest {

    @Test
    fun `point annotations with different type but same location are not equal`() {
        val triangle = _root_ide_package_.yi.core.go.GoAnnotation.Triangle(0, 0)
        val square = _root_ide_package_.yi.core.go.GoAnnotation.Square(0, 0)

        Assertions.assertNotEquals(triangle, square)
    }

    @Test
    fun `point annotations with same type and same location are equal`() {
        val triangle = _root_ide_package_.yi.core.go.GoAnnotation.Triangle(0, 0)
        val anotherTriangle = _root_ide_package_.yi.core.go.GoAnnotation.Triangle(0, 0)

        Assertions.assertEquals(triangle, anotherTriangle)
    }

    @Test
    fun `directional annotations with same type and same vector are equal`() {
        val line = _root_ide_package_.yi.core.go.GoAnnotation.Line(0, 0, 1, 1)
        val anotherLine = _root_ide_package_.yi.core.go.GoAnnotation.Line(0, 0, 1, 1)

        Assertions.assertEquals(line, anotherLine)
    }

    @Test
    fun `directional annotations with different type and same vector are not equal`() {
        val arrow = _root_ide_package_.yi.core.go.GoAnnotation.Arrow(0, 0, 1, 1)
        val line = _root_ide_package_.yi.core.go.GoAnnotation.Line(0, 0, 1, 1)

        Assertions.assertNotEquals(arrow, line)
    }

    @Test
    fun `directional annotations with same type but inverted direction are not equal`() {
        val line = _root_ide_package_.yi.core.go.GoAnnotation.Arrow(0, 0, 1, 1)
        val anotherLine = _root_ide_package_.yi.core.go.GoAnnotation.Line(1, 1, 0, 0)

        Assertions.assertNotEquals(line, anotherLine)
    }

}