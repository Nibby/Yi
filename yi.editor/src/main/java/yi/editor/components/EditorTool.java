package yi.editor.components;

import javafx.scene.control.ToggleGroup;
import yi.component.boardviewer.GameBoardViewer;
import yi.component.boardviewer.editmodes.AnnotationEditMode;
import yi.component.boardviewer.editmodes.EditMode;
import yi.component.boardviewer.editmodes.PlayMoveEditMode;
import yi.component.boardviewer.editmodes.StoneEditMode;
import yi.core.go.AnnotationType;
import yi.core.go.StoneColor;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.action.EditorToolAction;

import java.util.Optional;

/**
 * List of supported edit operations.
 */
public enum EditorTool {

    PLAY_MOVE {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.playMove());
        }

        @Override
        public EditorToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return EditorToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    EditorTextResources.TOOL_PLAY_MOVE,
                    "playMove",
                    EditorAcceleratorId.TOOL_PLAY_MOVE,
                    editMode -> editMode instanceof PlayMoveEditMode,
                    0d
            );
        }
    },

    ADD_BLACK_STONE {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.editStones(StoneColor.BLACK));
        }

        @Override
        public EditorToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return EditorToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    EditorTextResources.TOOL_ADD_BLACK,
                    "addBlack",
                    null,
                    editMode -> {
                        if (!(editMode instanceof StoneEditMode)) {
                            return false;
                        }
                        var stoneEditMode = (StoneEditMode) editMode;
                        return stoneEditMode.getColorToEdit() == StoneColor.BLACK;
                    },
                    0.01d
            );
        }
    },

    ADD_WHITE_STONE {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.editStones(StoneColor.WHITE));
        }

        @Override
        public EditorToolAction createAction(ToggleGroup toggleGroup, ToggleGroup menuGroup) {
            return EditorToolAction.forTool(
                    this,
                    toggleGroup,
                    menuGroup,
                    EditorTextResources.TOOL_ADD_WHITE,
                    "addWhite",
                    null,
                    editMode -> {
                        if (!(editMode instanceof StoneEditMode)) {
                            return false;
                        }
                        var stoneEditMode = (StoneEditMode) editMode;
                        return stoneEditMode.getColorToEdit() == StoneColor.WHITE;
                    },
                    0.02d
            );
        }
    },

    ANNOTATE_CROSS {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.annotation(AnnotationType.CROSS));
        }

        @Override
        public EditorToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return EditorToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    EditorTextResources.TOOL_CROSS,
                    "cross",
                    EditorAcceleratorId.TOOL_CROSS,
                    editMode -> {
                        if (!(editMode instanceof AnnotationEditMode)) {
                            return false;
                        }
                        var annotationMode = (AnnotationEditMode) editMode;
                        return annotationMode.getTypeToApply() == AnnotationType.CROSS;
                    },
                    0.03d
            );
        }
    },

    ANNOTATE_TRIANGLE {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.annotation(AnnotationType.TRIANGLE));
        }

        @Override
        public EditorToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
                return EditorToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    EditorTextResources.TOOL_TRIANGLE,
                    "triangle",
                    EditorAcceleratorId.TOOL_TRIANGLE,
                    editMode -> {
                        if (!(editMode instanceof AnnotationEditMode)) {
                            return false;
                        }
                        var annotationMode = (AnnotationEditMode) editMode;
                        return annotationMode.getTypeToApply() == AnnotationType.TRIANGLE;
                    },
                    0.04d
            );
        }
    },

    ANNOTATE_SQUARE {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.annotation(AnnotationType.SQUARE));
        }

        @Override
        public EditorToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
                return EditorToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    EditorTextResources.TOOL_SQUARE,
                    "square",
                    EditorAcceleratorId.TOOL_SQUARE,
                    editMode -> {
                        if (!(editMode instanceof AnnotationEditMode)) {
                            return false;
                        }
                        var annotationMode = (AnnotationEditMode) editMode;
                        return annotationMode.getTypeToApply() == AnnotationType.SQUARE;
                    },
                    0.05d
                );
        }
    },

    ANNOTATE_CIRCLE {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.annotation(AnnotationType.CIRCLE));
        }

        @Override
        public EditorToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return EditorToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    EditorTextResources.TOOL_CIRCLE,
                    "circle",
                    EditorAcceleratorId.TOOL_CIRCLE,
                    editMode -> {
                        if (!(editMode instanceof AnnotationEditMode)) {
                            return false;
                        }
                        var annotationMode = (AnnotationEditMode) editMode;
                        return annotationMode.getTypeToApply() == AnnotationType.CIRCLE;
                    },
                    0.06d
            );
        }
    },

    ANNOTATE_LETTER {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.annotationLabel(AnnotationEditMode.LabelType.LETTER));
        }

        @Override
        public EditorToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return EditorToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    EditorTextResources.TOOL_LABEL_LETTER,
                    "letter",
                    EditorAcceleratorId.TOOL_LABEL_LETTER,
                    editMode -> {
                        if (!(editMode instanceof AnnotationEditMode)) {
                            return false;
                        }
                        var annotationMode = (AnnotationEditMode) editMode;
                        if (annotationMode.getTypeToApply() != AnnotationType.LABEL) {
                            return false;
                        }
                        Optional<AnnotationEditMode.LabelType> labelType = annotationMode.getLabelType();
                        assert labelType.isPresent();
                        return labelType.get() == AnnotationEditMode.LabelType.LETTER;
                    },
                    0.07d
            );
        }
    },

    ANNOTATE_NUMBER {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.annotationLabel(AnnotationEditMode.LabelType.NUMBER));
        }

        @Override
        public EditorToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return EditorToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    EditorTextResources.TOOL_LABEL_NUMBER,
                    "number",
                    EditorAcceleratorId.TOOL_LABEL_NUMBER,
                    editMode -> {
                        if (!(editMode instanceof AnnotationEditMode)) {
                            return false;
                        }
                        var annotationMode = (AnnotationEditMode) editMode;
                        if (annotationMode.getTypeToApply() != AnnotationType.LABEL) {
                            return false;
                        }
                        Optional<AnnotationEditMode.LabelType> labelType = annotationMode.getLabelType();
                        assert labelType.isPresent();
                        return labelType.get() == AnnotationEditMode.LabelType.NUMBER;
                    },
                    0.08d
            );
        }
    },

    ANNOTATE_DIM {
        @Override
        public void apply(GameBoardViewer board) {
            var mode = new AnnotationEditMode(AnnotationType.DIM);
            board.setEditMode(mode);
        }

        @Override
        public EditorToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return EditorToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    EditorTextResources.TOOL_DIM,
                    "dim",
                    null,
                    editMode -> {
                        if (!(editMode instanceof AnnotationEditMode)) {
                            return false;
                        }
                        var annotationMode = (AnnotationEditMode) editMode;
                        return annotationMode.getTypeToApply() == AnnotationType.DIM;
                    },
                    0.09d
            );
        }
    },

    ANNOTATE_LINE {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.annotation(AnnotationType.LINE));
        }

        @Override
        public EditorToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return EditorToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    EditorTextResources.TOOL_LINE,
                    "line",
                    EditorAcceleratorId.TOOL_LINE,
                    editMode -> {
                        if (!(editMode instanceof AnnotationEditMode)) {
                            return false;
                        }
                        var annotationMode = (AnnotationEditMode) editMode;
                        return annotationMode.getTypeToApply() == AnnotationType.LINE;
                    },
                    0.10d
            );
        }
    },

    ANNOTATE_ARROW {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.annotation(AnnotationType.ARROW));
        }

        @Override
        public EditorToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return EditorToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    EditorTextResources.TOOL_ARROW,
                    "arrow",
                    EditorAcceleratorId.TOOL_ARROW,
                    editMode -> {
                        if (!(editMode instanceof AnnotationEditMode)) {
                            return false;
                        }
                        var annotationMode = (AnnotationEditMode) editMode;
                        return annotationMode.getTypeToApply() == AnnotationType.ARROW;
                    },
                    0.11d
            );
        }
    };

    public abstract void apply(GameBoardViewer board);

    public abstract EditorToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup);
}
