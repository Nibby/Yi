package yi.component.board.edits;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.board.AnnotationRenderer;
import yi.component.board.GameBoardManager;
import yi.core.go.Annotation;
import yi.core.go.AnnotationType;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class AnnotationEditMode implements EditMode {

    /**
     * Defines the behaviour for mouse drag across the board intersection.
     */
    private enum DragAction {
        /**
         * Each grid visited should have an annotation added there.
         */
        CREATE,

        /**
         * Each grid visited should have its annotation erased.
         */
        DELETE
    }

    /**
     * The internal game model does not differentiate between a letter label and a number label.
     * These are arbitrary differentiations made by the editor. Therefore we distinguish it here.
     */
    private enum LabelType {
        LETTER,
        NUMBER
    }

    private final AnnotationType typeToApply;

    // Internal
    private DragAction dragAction = DragAction.CREATE;

    // For label annotations only
    private static final String LETTER_LABEL_PROGRESSION = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private @Nullable LabelType labelType;
    private String nextLabelText;

    public AnnotationEditMode(AnnotationType typeToApply) {
        this.typeToApply = Objects.requireNonNull(typeToApply);
        this.labelType = null;
    }

    public AnnotationEditMode(@NotNull LabelType labelType) {
        this(AnnotationType.LABEL);

        this.labelType = Objects.requireNonNull(labelType);
    }

    @Override
    public void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY) {
        // TODO: Figure out how to draw label annotations here...
        Annotation temporary = null;

        if (AnnotationType.Companion.isPointAnnotation(typeToApply)) {
            temporary = Annotation.Companion.createFromType(typeToApply, gridX, gridY, -1, -1, getNextLabelText(manager));
        } else if (AnnotationType.Companion.isDirectionalAnnotation(typeToApply)) {
            if (directionalAnnoStartPositionDefined) {
                temporary = Annotation.Companion.createFromType(typeToApply, directionalAnnoFirstX, directionalAnnoFirstY, gridX, gridY, "");
            }
        } else {
            throw new IllegalStateException("Unsupported annotation type: " + typeToApply);
        }

        if (temporary != null) {
            g.setGlobalAlpha(0.5d);
            AnnotationRenderer.render(temporary, g, manager);
            g.setGlobalAlpha(1.0d);
        }
    }

    @Override
    public void onMousePress(GameBoardManager manager, int gridX, int gridY) {
        var currentState = manager.model.getCurrentGameState();
        var annotationHere = currentState.getAnnotation(gridX, gridY);

        if (annotationHere.isPresent()) {
            var itsType = annotationHere.get().getType();

            if (itsType == typeToApply) {
                dragAction = DragAction.DELETE;
                removeAnnotation(manager, gridX, gridY);
            } else {
                dragAction = DragAction.CREATE;
                maybeCreateAnnotation(manager, gridX, gridY);
            }
        } else {
            dragAction = DragAction.CREATE;
            maybeCreateAnnotation(manager, gridX, gridY);
        }
    }

    @Override
    public void onMouseDrag(GameBoardManager manager, int gridX, int gridY) {
        // TODO: Maybe allow re-positioning a directional annotation if it's dragged at either end?

        if (dragAction == DragAction.CREATE) {
            maybeCreateAnnotation(manager, gridX, gridY);
        } else if (dragAction == DragAction.DELETE) {
            removeAnnotation(manager, gridX, gridY);
        } else {
            throw new IllegalStateException("Unsupported drag action: " + dragAction.name());
        }
    }

    @Override
    public void onKeyPress(GameBoardManager manager, KeyEvent e) {

    }

    private void removeAnnotation(GameBoardManager manager, int gridX, int gridY) {
        manager.model.removeAnnotationsOnCurrentMove(gridX, gridY);
    }

    /*
        Directional annotations require two clicks, so it's not guaranteed that an annotation will be
        created by calling this method once.
     */
    private int directionalAnnoFirstX; // The first co-ordinate to create a directional annotation.
    private int directionalAnnoFirstY;
    private boolean directionalAnnoStartPositionDefined = false;
    
    private void maybeCreateAnnotation(GameBoardManager manager, int gridX, int gridY) {
        // First clear whatever annotation that is already here.
        // Only want one annotation per intersection.
        removeAnnotation(manager, gridX, gridY);

        if (AnnotationType.Companion.isPointAnnotation(typeToApply)) {
            createPointAnnotation(manager, typeToApply, gridX, gridY);
        } else if (AnnotationType.Companion.isDirectionalAnnotation(typeToApply)) {
            if (!directionalAnnoStartPositionDefined) {
                directionalAnnoFirstX = gridX;
                directionalAnnoFirstY = gridY;
                directionalAnnoStartPositionDefined = true;
            } else {
                assert directionalAnnoFirstX >= 0 && directionalAnnoFirstY >= 0
                        : String.format("Invalid directional annotation starting point: (%d, %d).", directionalAnnoFirstX, directionalAnnoFirstY);

                createDirectionalAnnotation(manager, typeToApply, directionalAnnoFirstX, directionalAnnoFirstY, gridX, gridY);
                resetDirectionalAnnotationCreateStep();
            }
        }
    }

    static void createPointAnnotation(GameBoardManager manager, AnnotationType annotationType, int gridX, int gridY) {
        // TODO: Label annotations?
        var pointAnno = Annotation.Companion.createFromType(annotationType, gridX, gridY, -1, -1, "");
        manager.model.addAnnotationToCurrentMove(pointAnno);
    }

    static void createDirectionalAnnotation(GameBoardManager manager, AnnotationType annotationType, int startX, int startY, int endX, int endY) {
        var directionalAnno = Annotation.Companion.createFromType(annotationType, startX, startY, endX, endY, "");
        manager.model.addAnnotationToCurrentMove(directionalAnno);
    }

    private void resetDirectionalAnnotationCreateStep() {
        directionalAnnoStartPositionDefined = false;
        directionalAnnoFirstX = -1;
        directionalAnnoFirstY = -1;
    }

    private String getNextLabelText(GameBoardManager manager) {
        assertLabelTypeNotNull();

        if (nextLabelText == null) {
            computeNextLabelText(manager);
        }

        return nextLabelText;
    }

    private void computeNextLabelText(GameBoardManager manager) {
        assertLabelTypeNotNull();

        var allLabelText =
                manager.model.getAllAnnotationsOnCurrentMove()
                .stream()
                .filter(anno -> anno instanceof Annotation.Label)
                .map(anno -> ((Annotation.Label) anno).getText())
                .collect(Collectors.toSet());

        if (labelType == LabelType.LETTER) {
            computeNextLetterText(allLabelText);
        } else if (labelType == LabelType.NUMBER) {
            computeNextNumberText(allLabelText);
        } else throw new IllegalStateException("Unimplemented label type: " + Objects.requireNonNull(labelType).name());
    }

    private void computeNextNumberText(Set<String> allLabelText) {

    }

    private void computeNextLetterText(Set<String> allLabelText) {
        // TODO: Make some magic happen.
        String current;
        int step = 0;

        for (;;) {
            current = String.valueOf(LETTER_LABEL_PROGRESSION.charAt(step));

            if (allLabelText.contains(current)) {
                ++step;
            }

        }
    }

    private void assertLabelTypeNotNull() {
        if (labelType == null)
            throw new IllegalStateException("Bad internal state. Should not be retrieving label text if labelType is null.");
    }
}
