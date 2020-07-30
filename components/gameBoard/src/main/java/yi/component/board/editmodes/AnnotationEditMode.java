package yi.component.board.editmodes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.board.AnnotationRenderer;
import yi.component.board.GameBoardManager;
import yi.core.go.Annotation;
import yi.core.go.AnnotationType;

import java.util.*;
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
    public enum LabelType {
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
        if (manager.model.getCurrentGameState().getAnnotation(gridX, gridY).isPresent()) {
            return;
        }

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
    public void onMousePress(MouseButton button, GameBoardManager manager, int gridX, int gridY) {
        var currentState = manager.model.getCurrentGameState();
        var annotationHere = currentState.getAnnotation(gridX, gridY);

        boolean createRatherThanDelete;

        if (annotationHere.isPresent()) {
            var itsType = annotationHere.get().getType();

            createRatherThanDelete = itsType != typeToApply;
        } else {
            createRatherThanDelete = true;
        }

        if (createRatherThanDelete) {
            dragAction = DragAction.CREATE;
            maybeCreateAnnotation(manager, gridX, gridY);
        } else {
            dragAction = DragAction.DELETE;
            removeAnnotation(manager, gridX, gridY);
        }
    }

    @Override
    public void onMouseDrag(MouseButton button, GameBoardManager manager, int gridX, int gridY) {
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

    @Override
    public void onMouseRelease(MouseButton button, GameBoardManager manager, int cursorX, int cursorY) {
        if (typeToApply == AnnotationType.LABEL) {
            computeNextLabelText(manager);
        }
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
            createPointAnnotation(manager, typeToApply, gridX, gridY, nextLabelText);
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

    static void createPointAnnotation(GameBoardManager manager, AnnotationType annotationType, int gridX, int gridY, String nextLabelText) {
        if (!AnnotationType.Companion.isPointAnnotation(annotationType)) {
            throw new IllegalArgumentException("Not a point annotation: " + annotationType.name());
        }

        var annotationText = nextLabelText == null ? "" : nextLabelText;
        Annotation pointAnno = Annotation.Companion.createFromType(annotationType, gridX, gridY, -1, -1, annotationText);
        manager.model.addAnnotationToCurrentMove(pointAnno);
    }

    static void createDirectionalAnnotation(GameBoardManager manager, AnnotationType annotationType, int startX, int startY, int endX, int endY) {
        if (!AnnotationType.Companion.isDirectionalAnnotation(annotationType)) {
            throw new IllegalArgumentException("Not a directional annotation: " + annotationType.name());
        }

        var directionalAnno = Annotation.Companion.createFromType(annotationType, startX, startY, endX, endY, "");
        manager.model.addAnnotationToCurrentMove(directionalAnno);
    }

    private void resetDirectionalAnnotationCreateStep() {
        directionalAnnoStartPositionDefined = false;
        directionalAnnoFirstX = -1;
        directionalAnnoFirstY = -1;
    }

    private String getNextLabelText(GameBoardManager manager) {
        if (labelType == null)
            return "";

        if (nextLabelText == null) {
            computeNextLabelText(manager);
        }

        return nextLabelText;
    }

    private void computeNextLabelText(GameBoardManager manager) {
        assertLabelTypeNotNull();

        if (labelType == LabelType.LETTER) {
            var annoTexts =
                    manager.model.getAllAnnotationsOnCurrentMove()
                    .stream()
                    .filter(anno -> anno instanceof Annotation.Label)
                    .map(anno -> ((Annotation.Label) anno).getText())
                    .filter(text -> !NumberUtils.isCreatable(text))
                    .collect(Collectors.toSet());

            int step = 0;
            String candidateText;

            // TODO: This is not ideal. Annotation labels should ideally correspond to a base 26 format so that
            //       if the annotation exceeds letter Z, we append AA, AB ... AZ, then AAA, AAB ... etc.

            do {
                candidateText = String.valueOf(LETTER_LABEL_PROGRESSION.charAt(step));
                if (annoTexts.contains(candidateText)) {
                    ++step;
                } else {
                    break;
                }
            } while (step < LETTER_LABEL_PROGRESSION.length());

            nextLabelText = candidateText;
        }
        else if (labelType == LabelType.NUMBER) {
            var maxNumber =
                manager.model.getAllAnnotationsOnCurrentMove()
                    .stream()
                    .filter(anno -> anno instanceof Annotation.Label)
                    .map(anno -> ((Annotation.Label) anno).getText())
                    .filter(NumberUtils::isCreatable)
                    .map(Integer::parseInt)
                    .max((a, b) -> a > b ? a : (a.equals(b)) ? 0 : -1);

            nextLabelText = String.valueOf(1 + maxNumber.orElse(0));
        } else throw new IllegalStateException("Unimplemented label type: " + Objects.requireNonNull(labelType).name());
    }

    private void assertLabelTypeNotNull() {
        if (labelType == null)
            throw new IllegalStateException("Bad internal state. Should not be retrieving label text if labelType is null.");
    }
}
