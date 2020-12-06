package yi.editor;

import javafx.scene.control.ToggleGroup;
import org.jetbrains.annotations.Nullable;
import yi.common.i18n.TextResource;
import yi.component.board.GameBoardViewer;
import yi.component.board.editmodes.*;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionContext;
import yi.editor.framework.action.EditorRadioAction;
import yi.models.go.AnnotationType;
import yi.models.go.StoneColor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

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
        public EditorRadioAction createAction(ToggleGroup toggleGroup) {
            return EditorTool.createToolAction(
                this,
                toggleGroup,
                EditorTextResources.TOOL_PLAY_MOVE,
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
        public EditorRadioAction createAction(ToggleGroup toggleGroup) {
            return EditorTool.createToolAction(
                this,
                toggleGroup,
                EditorTextResources.TOOL_ADD_BLACK,
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
        public EditorRadioAction createAction(ToggleGroup toggleGroup) {
            return EditorTool.createToolAction(
                this,
                toggleGroup,
                EditorTextResources.TOOL_ADD_WHITE,
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
        public EditorRadioAction createAction(ToggleGroup toggleGroup) {
            return EditorTool.createToolAction(
                    this,
                    toggleGroup,
                    EditorTextResources.TOOL_CROSS,
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
        public EditorRadioAction createAction(ToggleGroup toggleGroup) {
            return EditorTool.createToolAction(
                this,
                toggleGroup,
                EditorTextResources.TOOL_TRIANGLE,
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
        public EditorRadioAction createAction(ToggleGroup toggleGroup) {
            return EditorTool.createToolAction(
                this,
                toggleGroup,
                EditorTextResources.TOOL_SQUARE,
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
        public EditorRadioAction createAction(ToggleGroup toggleGroup) {
            return EditorTool.createToolAction(
                    this,
                    toggleGroup,
                    EditorTextResources.TOOL_CIRCLE,
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
        public EditorRadioAction createAction(ToggleGroup toggleGroup) {
            return EditorTool.createToolAction(
                this,
                toggleGroup,
                EditorTextResources.TOOL_LABEL_LETTER,
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
        public EditorRadioAction createAction(ToggleGroup toggleGroup) {
            return EditorTool.createToolAction(
                this,
                toggleGroup,
                EditorTextResources.TOOL_LABEL_NUMBER,
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

    ANNOTATE_LINE {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.annotation(AnnotationType.LINE));
        }

        @Override
        public EditorRadioAction createAction(ToggleGroup toggleGroup) {
            return EditorTool.createToolAction(
                this,
                toggleGroup,
                EditorTextResources.TOOL_LINE,
                EditorAcceleratorId.TOOL_LINE,
                editMode -> {
                    if (!(editMode instanceof AnnotationEditMode)) {
                        return false;
                    }
                    var annotationMode = (AnnotationEditMode) editMode;
                    return annotationMode.getTypeToApply() == AnnotationType.LINE;
                },
                0.09d
            );
        }
    },

    ANNOTATE_ARROW {
        @Override
        public void apply(GameBoardViewer board) {
            board.setEditMode(EditMode.annotation(AnnotationType.ARROW));
        }

        @Override
        public EditorRadioAction createAction(ToggleGroup toggleGroup) {
            return EditorTool.createToolAction(
                this,
                toggleGroup,
                EditorTextResources.TOOL_ARROW,
                EditorAcceleratorId.TOOL_ARROW,
                editMode -> {
                    if (!(editMode instanceof AnnotationEditMode)) {
                        return false;
                    }
                    var annotationMode = (AnnotationEditMode) editMode;
                    return annotationMode.getTypeToApply() == AnnotationType.ARROW;
                },
                0.1d
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
        public EditorRadioAction createAction(ToggleGroup toggleGroup) {
            return EditorTool.createToolAction(
                this,
                toggleGroup,
                EditorTextResources.TOOL_DIM,
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
    };

    public abstract void apply(GameBoardViewer board);

    public abstract EditorRadioAction createAction(ToggleGroup toggleGroup);

    private static GameBoardViewer getGameBoardViewer(EditorActionContext context) {
        var window = context.getEditorWindow();
        var board  = window.getBoardArea();
        return board.getGameBoardViewer();
    }

    private static EditorRadioAction createToolAction(EditorTool tool,
                                                      ToggleGroup group,
                                                      TextResource label,
                                                      @Nullable EditorAcceleratorId acceleratorId,
                                                      Predicate<AbstractEditMode> selectionCriteria,
                                                      double menuPosition) {

        var action = new EditorRadioAction(label,
                context -> tool.apply(getGameBoardViewer(context))) {
            @Override
            public void refreshState(EditorActionContext context) {
                super.refreshState(context);
                var boardViewer = getGameBoardViewer(context);
                var mode = boardViewer.getEditMode();
                var selected = selectionCriteria.test(mode);
                setSelected(selected);
            }
        };
        action.setInMainMenu(EditorMainMenuType.TOOLS, menuPosition);
        if (acceleratorId != null) {
            action.setAccelerator(acceleratorId);
        }
        action.setMenuToggleGroup(group);
        return action;
    }

    public static Set<EditorAction> createActionSet() {
        Set<EditorAction> actions = new HashSet<>();
        var group = new ToggleGroup();
        for (EditorTool tool : values()) {
            actions.add(tool.createAction(group));
        }
        return actions;
    }
}
