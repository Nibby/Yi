package yi.component.board.editmodes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yi.component.board.GameBoardClassFactory;
import yi.component.board.edits.AnnotationEdit;
import yi.core.go.Annotation;
import yi.core.go.GameModel;
import yi.core.go.GameRules;

public final class AnnotationsEditModeMechanismTest {

    @Test
    public void testManyAdditions_InOneSession_MergedIntoMostRecentEdit() {
        var model = new GameModel(3, 3, GameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        manager.setGameModel(model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(3);

        // Method under test
        final long sessionId = 0;
        var item1 = AnnotationEdit.forNew(manager.model.getCurrentMove(), new Annotation.Triangle(0, 0), sessionId);
        var item2 = AnnotationEdit.forNew(manager.model.getCurrentMove(), new Annotation.Triangle(1, 0), sessionId);
        var item3 = AnnotationEdit.forNew(manager.model.getCurrentMove(), new Annotation.Triangle(2, 0), sessionId);

        editor.recordAndApply(item1, manager);
        editor.recordAndApply(item2, manager);
        editor.recordAndApply(item3, manager);

        Assertions.assertEquals(1, editor.getCurrentHistorySize());
        Assertions.assertEquals(item1, editor.getHistoryItem(0), "item2 and item3 should be merged into item1 because they are in the same session.");
    }

    @Test
    public void testManyDeletions_InOneSession_MergedIntoMostRecentEdit() {
        var model = new GameModel(3, 3, GameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        manager.setGameModel(model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(3);

        final long sessionId = 0;
        var item1 = AnnotationEdit.forRemoval(manager.model.getCurrentMove(), new Annotation.Triangle(0, 0), sessionId);
        var item2 = AnnotationEdit.forRemoval(manager.model.getCurrentMove(), new Annotation.Triangle(1, 0), sessionId);
        var item3 = AnnotationEdit.forRemoval(manager.model.getCurrentMove(), new Annotation.Triangle(2, 0), sessionId);

        // Method under test
        editor.recordAndApply(item1, manager);
        editor.recordAndApply(item2, manager);
        editor.recordAndApply(item3, manager);

        Assertions.assertEquals(1, editor.getCurrentHistorySize());
        Assertions.assertEquals(item1, editor.getHistoryItem(0), "item2 and item3 should be merged into item1 because they are in the same session.");
    }

}
