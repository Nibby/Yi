package yi.models.go

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AnnotationTypeCoverageTest {

    @Test
    fun `All annotation types are covered by type checks`() {
        // Annotation types must be either point or directional, or covered by some other type-determining method.
        // This test only checks for the two specific cases above.
        for (annotationType in AnnotationType.values()) {
            val directional = AnnotationType.isDirectionalAnnotation(annotationType)
            val point = AnnotationType.isPointAnnotation(annotationType)

            Assertions.assertTrue(point || directional, annotationType.name + " is not covered by type check!")
        }
    }
}