package yi.core.go

import java.util.*

abstract class GoAnnotation constructor(val type: _root_ide_package_.yi.core.go.GoAnnotationType, val x: Int, val y: Int) {

    abstract class PointAnnotation(type: _root_ide_package_.yi.core.go.GoAnnotationType, x: Int, y: Int) : _root_ide_package_.yi.core.go.GoAnnotation(type, x, y) {

        override fun equals(other: Any?): Boolean {
            other?.let {
                if (other is _root_ide_package_.yi.core.go.GoAnnotation.PointAnnotation) {
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

    class Triangle(x: Int, y: Int) : _root_ide_package_.yi.core.go.GoAnnotation.PointAnnotation(_root_ide_package_.yi.core.go.GoAnnotationType.TRIANGLE, x, y)

    class Square(x: Int, y: Int) : _root_ide_package_.yi.core.go.GoAnnotation.PointAnnotation(_root_ide_package_.yi.core.go.GoAnnotationType.SQUARE, x, y)

    class Circle(x: Int, y: Int) : _root_ide_package_.yi.core.go.GoAnnotation.PointAnnotation(_root_ide_package_.yi.core.go.GoAnnotationType.CIRCLE, x, y)

    class Cross(x: Int, y: Int) : _root_ide_package_.yi.core.go.GoAnnotation.PointAnnotation(_root_ide_package_.yi.core.go.GoAnnotationType.CROSS, x, y)

    class Fade(x: Int, y: Int) : _root_ide_package_.yi.core.go.GoAnnotation.PointAnnotation(_root_ide_package_.yi.core.go.GoAnnotationType.FADE, x, y)


    abstract class DirectionalAnnotation(type: _root_ide_package_.yi.core.go.GoAnnotationType, x: Int, y: Int, val xEnd: Int, val yEnd: Int) : _root_ide_package_.yi.core.go.GoAnnotation(type, x, y) {
        override fun equals(other: Any?): Boolean {
            other?.let {
                if (other is _root_ide_package_.yi.core.go.GoAnnotation.DirectionalAnnotation) {
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

    class Line(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int) : _root_ide_package_.yi.core.go.GoAnnotation.DirectionalAnnotation(_root_ide_package_.yi.core.go.GoAnnotationType.LINE, xStart, yStart, xEnd, yEnd)

    class Arrow(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int) : _root_ide_package_.yi.core.go.GoAnnotation.DirectionalAnnotation(_root_ide_package_.yi.core.go.GoAnnotationType.ARROW, xStart, yStart, xEnd, yEnd)

}