package yi.component.board.editmodes;

import yi.core.go.AnnotationType;

public final class EditMode {

    public static PlayMoveEditMode playMove() {
        return new PlayMoveEditMode();
    }

    public static AnnotationEditMode annotation(AnnotationType type) {
        if (type == AnnotationType.LABEL) {
            // Bad API design, sorry :(
            throw new IllegalArgumentException("Direct usage of LABEL type is not supported. Use EditMode.annotationLabel(AnnotationEditMode.LabelType) instead.");
        }

        return new AnnotationEditMode(type);
    }

    public static AnnotationEditMode annotationLabel(AnnotationEditMode.LabelType labelType) {
        return new AnnotationEditMode(labelType);
    }
}
