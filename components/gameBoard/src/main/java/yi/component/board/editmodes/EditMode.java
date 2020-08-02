package yi.component.board.editmodes;

import yi.core.go.AnnotationType;

public final class EditMode {

    public static PlayMoveEditMode playMove() {
        return new PlayMoveEditMode();
    }

    public static AnnotationEditMode annotation(AnnotationType type) {
        return new AnnotationEditMode(type);
    }

    public static AnnotationEditMode annotationLabel(AnnotationEditMode.LabelType labelType) {
        return new AnnotationEditMode(labelType);
    }
}
