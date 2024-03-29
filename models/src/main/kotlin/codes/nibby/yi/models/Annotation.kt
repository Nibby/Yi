package codes.nibby.yi.models

import codes.nibby.yi.models.Annotation.DirectionalAnnotation
import codes.nibby.yi.models.Annotation.PointAnnotation
import java.util.*

/**
 * Annotations are special labels that can be added to a [GameNode] through
 * [GameModel.addAnnotationToCurrentNode].
 *
 * Unlike other game objects, annotations do not accumulate. They are only present on the
 * game node it is added to. Annotation positions use the same co-ordinate space as the
 * game board.
 *
 * There are two types of annotations:
 *
 * [PointAnnotation] are those that are added to a single grid position, while
 * [DirectionalAnnotation] are two-point annotations that have a start point and an
 * end point like vectors.
 *
 * This implementation is compliant with the specifications laid out by SGF-4 standard.
 */
abstract class Annotation constructor(val type: AnnotationType, val x: Int, val y: Int) {

    /**
     * Whether this annotation is occupying the point specified.
     *
     * @return true if the annotation is occupying this point.
     */
    abstract fun isOccupyingPosition(x: Int, y: Int): Boolean

    /**
     * Represents an annotation whose location can be modelled using a single co-ordinate.
     */
    abstract class PointAnnotation(type: AnnotationType, x: Int, y: Int) : Annotation(type, x, y) {

        override fun isOccupyingPosition(x: Int, y: Int): Boolean {
            return x == this.x && y == this.y
        }

        override fun equals(other: Any?): Boolean {
            other?.let {
                if (other is PointAnnotation) {
                    return this.type == other.type && this.x == other.x && this.y == other.y
                }
            }
            return super.equals(other)
        }

        override fun hashCode(): Int {
            return Objects.hash(type, x, y)
        }

        override fun toString(): String {
            return "$type at ($x, $y)"
        }
    }

    class Triangle(x: Int, y: Int) : PointAnnotation(AnnotationType.TRIANGLE, x, y)

    class Square(x: Int, y: Int) : PointAnnotation(AnnotationType.SQUARE, x, y)

    class Circle(x: Int, y: Int) : PointAnnotation(AnnotationType.CIRCLE, x, y)

    class Cross(x: Int, y: Int) : PointAnnotation(AnnotationType.CROSS, x, y)

    class Label(x: Int, y: Int, val text: String) : PointAnnotation(
        AnnotationType.LABEL, x, y)

    class Fade(x: Int, y: Int) : PointAnnotation(AnnotationType.DIM, x, y)

    /**
     * This is not part of the SGF-4 standard. Rather, it is an internal annotation
     * used by the program only. This annotation will not be saved to file.
     */
    class Dot(x: Int, y: Int) : PointAnnotation(AnnotationType.DOT, x, y)


    /**
     * Represents an annotation whose location is modelled by two points, and has a direction.
     */
    abstract class DirectionalAnnotation(type: AnnotationType, x: Int, y: Int,
                                         val xEnd: Int, val yEnd: Int) : Annotation(type, x, y) {

        override fun isOccupyingPosition(x: Int, y: Int): Boolean {
            return this.x == x && this.y == y || this.xEnd == x  && this.yEnd == y
        }

        override fun equals(other: Any?): Boolean {
            other?.let {
                if (other is DirectionalAnnotation) {
                    return this.type == other.type && this.x == other.x && this.y == other.y
                            && this.xEnd == other.xEnd && this.yEnd == other.yEnd
                }
            }

            return false
        }

        override fun hashCode(): Int {
            return Objects.hash(type, x, y, xEnd, yEnd)
        }

        override fun toString(): String {
            return "$type from ($x, $y) to ($xEnd, $yEnd)"
        }
    }

    class Line(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int)
        : DirectionalAnnotation(AnnotationType.LINE, xStart, yStart, xEnd, yEnd)

    class Arrow(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int)
        : DirectionalAnnotation(AnnotationType.ARROW, xStart, yStart, xEnd, yEnd)

    companion object {

        /**
         * Factory method: creates an annotation from a given annotation type at the
         * target (x, y) co-ordinate.
         *
         * @throws IllegalArgumentException If the annotation type is not a supported
         * [PointAnnotation]
         */
        fun createFromType(type: AnnotationType, x1: Int, y1: Int, x2: Int, y2: Int,
                           text: String): Annotation {
            return when (type) {
                AnnotationType.DOT -> Dot(x1, y1)

                AnnotationType.TRIANGLE -> Triangle(x1, y1)
                AnnotationType.CIRCLE -> Circle(x1, y1)
                AnnotationType.SQUARE -> Square(x1, y1)
                AnnotationType.CROSS -> Cross(x1, y1)
                AnnotationType.DIM -> Fade(x1, y1)
                AnnotationType.LABEL -> Label(x1, y1, text)

                AnnotationType.LINE -> Line(x1, y1, x2, y2)
                AnnotationType.ARROW -> Arrow(x1, y1, x2, y2)
            }
        }
    }
}