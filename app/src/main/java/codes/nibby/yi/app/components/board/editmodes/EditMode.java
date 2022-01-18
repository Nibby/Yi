package codes.nibby.yi.app.components.board.editmodes;

import codes.nibby.yi.models.AnnotationType;
import codes.nibby.yi.models.StoneColor;

/**
 * Factory class that instantiates various {@link AbstractEditMode} types.
 */
public final class EditMode {

    private EditMode() {
        // Factory class, no instantiation
    }

    /**
     * @return Edit mode type to submit game moves.
     */
    public static PlayMoveEditMode playMove() {
        return new PlayMoveEditMode();
    }

    /**
     * @param type Type of annotation that is <b>not</b> a label.
     * @return Edit mode type to manipulate non-labelled annotation data.
     */
    public static AnnotationEditMode annotation(AnnotationType type) {
        if (type == AnnotationType.LABEL) {
            // Bad API design, sorry :(
            throw new IllegalArgumentException("Direct usage of LABEL type is not supported. " +
                    "Use EditMode.annotationLabel(AnnotationEditMode.LabelType) instead.");
        }

        return new AnnotationEditMode(type);
    }

    /**
     * @param labelType Type of label annotation.
     * @return Edit mode type to manipulate label annotation data.
     */
    public static AnnotationEditMode annotationLabel(AnnotationEditMode.LabelType labelType) {
        return new AnnotationEditMode(labelType);
    }

    /**
     * @param stoneColor Stone color.
     * @return Edit mode to add or remove stones in a board position outside of the
     * game rules.
     */
    public static StoneEditMode editStones(StoneColor stoneColor) {
        return new StoneEditMode(stoneColor);
    }
}
