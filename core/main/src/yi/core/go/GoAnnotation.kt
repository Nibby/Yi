package yi.core.go

import java.util.*

abstract class GoAnnotation constructor(val type: GoAnnotationType, val x: Int, val y: Int) {

    abstract class PointAnnotation(type: GoAnnotationType, x: Int, y: Int) : GoAnnotation(type, x, y) {

        override fun equals(other: Any?): Boolean {
            other?.let {
                if (other is GoAnnotation.PointAnnotation) {
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

    class Triangle(x: Int, y: Int) : GoAnnotation.PointAnnotation(GoAnnotationType.TRIANGLE, x, y)

    class Square(x: Int, y: Int) : GoAnnotation.PointAnnotation(GoAnnotationType.SQUARE, x, y)

    class Circle(x: Int, y: Int) : GoAnnotation.PointAnnotation(GoAnnotationType.CIRCLE, x, y)

    class Cross(x: Int, y: Int) : GoAnnotation.PointAnnotation(GoAnnotationType.CROSS, x, y)

    class Fade(x: Int, y: Int) : GoAnnotation.PointAnnotation(GoAnnotationType.FADE, x, y)


    abstract class DirectionalAnnotation(type: GoAnnotationType, x: Int, y: Int, val xEnd: Int, val yEnd: Int) : GoAnnotation(type, x, y) {
        override fun equals(other: Any?): Boolean {
            other?.let {
                if (other is GoAnnotation.DirectionalAnnotation) {
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

    class Line(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int) : GoAnnotation.DirectionalAnnotation(GoAnnotationType.LINE, xStart, yStart, xEnd, yEnd)

    class Arrow(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int) : GoAnnotation.DirectionalAnnotation(GoAnnotationType.ARROW, xStart, yStart, xEnd, yEnd)

}