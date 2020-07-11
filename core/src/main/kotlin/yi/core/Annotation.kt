package yi.core

import java.util.*

abstract class Annotation constructor(val type: AnnotationType, val x: Int, val y: Int) {

    abstract class PointAnnotation(type: AnnotationType, x: Int, y: Int) : Annotation(type, x, y) {

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

    class Fade(x: Int, y: Int) : PointAnnotation(AnnotationType.FADE, x, y)


    abstract class DirectionalAnnotation(type: AnnotationType, x: Int, y: Int, val xEnd: Int, val yEnd: Int) : Annotation(type, x, y) {
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

    class Line(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int) : DirectionalAnnotation(AnnotationType.LINE, xStart, yStart, xEnd, yEnd)

    class Arrow(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int) : DirectionalAnnotation(AnnotationType.ARROW, xStart, yStart, xEnd, yEnd)

}