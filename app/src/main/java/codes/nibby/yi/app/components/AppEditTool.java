package codes.nibby.yi.app.components;

import codes.nibby.yi.app.framework.AppAccelerator;
import codes.nibby.yi.app.framework.AppText;
import codes.nibby.yi.app.framework.action.AppToolAction;
import javafx.scene.control.ToggleGroup;
import codes.nibby.yi.app.components.board.GameBoardViewer;
import codes.nibby.yi.app.components.board.editmodes.AnnotationEditMode;
import codes.nibby.yi.app.components.board.editmodes.EditMode;
import codes.nibby.yi.app.components.board.editmodes.PlayMoveEditMode;
import codes.nibby.yi.app.components.board.editmodes.StoneEditMode;
import codes.nibby.yi.models.AnnotationType;
import codes.nibby.yi.models.StoneColor;

import java.util.Optional;

/**
 * List of supported edit operations.
 */
public enum AppEditTool {

    PLAY_MOVE {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.playMove());
        }

        @Override
        public AppToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return AppToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    AppText.TOOL_PLAY_MOVE,
                    "playMove",
                    AppAccelerator.TOOL_PLAY_MOVE,
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
        public AppToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return AppToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    AppText.TOOL_ADD_BLACK,
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
        public AppToolAction createAction(ToggleGroup toggleGroup, ToggleGroup menuGroup) {
            return AppToolAction.forTool(
                    this,
                    toggleGroup,
                    menuGroup,
                    AppText.TOOL_ADD_WHITE,
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
        public AppToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return AppToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    AppText.TOOL_CROSS,
                    "cross",
                    AppAccelerator.TOOL_CROSS,
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
        public AppToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
                return AppToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    AppText.TOOL_TRIANGLE,
                    "triangle",
                    AppAccelerator.TOOL_TRIANGLE,
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
        public AppToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
                return AppToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    AppText.TOOL_SQUARE,
                    "square",
                    AppAccelerator.TOOL_SQUARE,
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
        public AppToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return AppToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    AppText.TOOL_CIRCLE,
                    "circle",
                    AppAccelerator.TOOL_CIRCLE,
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
        public AppToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return AppToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    AppText.TOOL_LABEL_LETTER,
                    "letter",
                    AppAccelerator.TOOL_LABEL_LETTER,
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
        public AppToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return AppToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    AppText.TOOL_LABEL_NUMBER,
                    "number",
                    AppAccelerator.TOOL_LABEL_NUMBER,
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
        public AppToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return AppToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    AppText.TOOL_DIM,
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
        public AppToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return AppToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    AppText.TOOL_LINE,
                    "line",
                    AppAccelerator.TOOL_LINE,
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
        public AppToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup) {
            return AppToolAction.forTool(
                    this,
                    componentGroup,
                    menuGroup,
                    AppText.TOOL_ARROW,
                    "arrow",
                    AppAccelerator.TOOL_ARROW,
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

    public abstract AppToolAction createAction(ToggleGroup componentGroup, ToggleGroup menuGroup);
}
