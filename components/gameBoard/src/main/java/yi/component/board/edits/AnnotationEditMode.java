package yi.component.board.edits;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import yi.component.board.AnnotationRenderer;
import yi.component.board.GameBoardManager;
import yi.core.go.Annotation;
import yi.core.go.AnnotationType;

public class AnnotationEditMode implements EditMode {

    private AnnotationType annotationType;

    public AnnotationEditMode(AnnotationType annotationType) {
        setAnnotationType(annotationType);
    }

    public AnnotationType getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(AnnotationType template) {
        this.annotationType = template;
    }

    @Override
    public void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY) {
        g.setGlobalAlpha(0.5d);
        var temporary = Annotation.Companion.fromType(annotationType, gridX, gridY, -1, -1, "");
        AnnotationRenderer.render(temporary, g, manager);
        g.setGlobalAlpha(1.0d);
    }

    @Override
    public void onMousePress(GameBoardManager manager, int gridX, int gridY) {

    }

    @Override
    public void onKeyPress(GameBoardManager manager, KeyEvent e) {

    }
}
