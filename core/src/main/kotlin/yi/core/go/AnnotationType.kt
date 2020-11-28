package yi.core.go

/**
 * Lists all the annotation shapes supported by the program.
 */
enum class AnnotationType {

    CIRCLE,
    TRIANGLE,
    SQUARE,
    CROSS,
    FADE,
    LABEL,
    LINE,
    ARROW,

    // Non-standard
    DOT;

    companion object {
        /**
         *
         * @return true if the annotation type is a directional annotation.
         */
        fun isDirectionalAnnotation(type: AnnotationType): Boolean {
            return when (type) {
                LINE, ARROW -> true
                else -> false
            }
        }

        /**
         *
         * @return true if the annotation type is a point annotation.
         */
        fun isPointAnnotation(type: AnnotationType): Boolean {
            return when (type) {
                DOT, CIRCLE, TRIANGLE, SQUARE, CROSS, FADE, LABEL -> true
                else -> false
            }
        }
    }

}