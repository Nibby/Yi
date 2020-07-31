package yi.editor;

import yi.component.board.GameBoardViewer;
import yi.component.board.editmodes.AnnotationEditMode;
import yi.component.board.editmodes.AbstractEditMode;
import yi.component.board.editmodes.EditMode;
import yi.core.go.AnnotationType;

/**
 * List of supported edit operations.
 */
public enum EditorTool {

    PLAY_MOVE {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.PLAY_MOVE);
        }
    },

    ADD_BLACK_STONE {
        @Override
        public void apply(GameBoardViewer board) {

        }
    },
    ADD_WHITE_STONE {
        @Override
        public void apply(GameBoardViewer board) {

        }
    },

    ANNOTATE_TRIANGLE {
        @Override
        public void apply(GameBoardViewer board) {
            var mode = new AnnotationEditMode(AnnotationType.TRIANGLE);
            board.setEditMode(mode);
        }
    },
    ANNOTATE_CIRCLE {
        @Override
        public void apply(GameBoardViewer board) {
            var mode = new AnnotationEditMode(AnnotationType.CIRCLE);
            board.setEditMode(mode);
        }
    },
    ANNOTATE_SQUARE {
        @Override
        public void apply(GameBoardViewer board) {
            var mode = new AnnotationEditMode(AnnotationType.SQUARE);
            board.setEditMode(mode);
        }
    },
    ANNOTATE_CROSS {
        @Override
        public void apply(GameBoardViewer board) {
            var mode = new AnnotationEditMode(AnnotationType.CROSS);
            board.setEditMode(mode);
        }
    },
    ANNOTATE_LETTER {
        @Override
        public void apply(GameBoardViewer board) {
            var mode = new AnnotationEditMode(AnnotationEditMode.LabelType.LETTER);
            board.setEditMode(mode);
        }
    },
    ANNOTATE_NUMBER {
        @Override
        public void apply(GameBoardViewer board) {
            var mode = new AnnotationEditMode(AnnotationEditMode.LabelType.NUMBER);
            board.setEditMode(mode);
        }
    },
    ANNOTATE_LINE {
        @Override
        public void apply(GameBoardViewer board) {
            var mode = new AnnotationEditMode(AnnotationType.LINE);
            board.setEditMode(mode);
        }
    },
    ANNOTATE_ARROW {
        @Override
        public void apply(GameBoardViewer board) {
            var mode = new AnnotationEditMode(AnnotationType.ARROW);
            board.setEditMode(mode);
        }
    },
    ANNOTATE_DIM {
        @Override
        public void apply(GameBoardViewer board) {
            var mode = new AnnotationEditMode(AnnotationType.FADE);
            board.setEditMode(mode);
        }
    };

    public abstract void apply(GameBoardViewer board);
}
