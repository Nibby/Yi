package yi.component.board.editmodes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yi.component.board.GameBoardClassFactory;
import yi.component.board.GameBoardManagerAccessor;
import yi.component.board.edits.AnnotationEdit;
import yi.models.go.Annotation;
import yi.models.go.GameModel;
import yi.models.go.StandardGameRules;

import java.util.HashSet;
import java.util.Stack;

public final class AnnotationEditModeUndoRedoTest {

    @Test
    public void testAddAnnotationCreatesHistoryItem() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(3);

        // Method under test
        var item1 = AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(0, 0), 0);
        var item2 = AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(0, 0), 1);
        var item3 = AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(0, 0), 2);

        editor.recordAndApply(item1, manager);
        editor.recordAndApply(item2, manager);
        editor.recordAndApply(item3, manager);

        Assertions.assertEquals(3, editor.getCurrentHistorySize());
        Assertions.assertEquals(item1, editor.getHistoryItem(0), "Incorrect undo stack state.");
        Assertions.assertEquals(item2, editor.getHistoryItem(1), "Incorrect undo stack state.");
        Assertions.assertEquals(item3, editor.getHistoryItem(2), "Incorrect undo stack state.");
    }

    @Test
    public void testRemoveAnnotationCreatesHistoryItem() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(3);

        // Method under test
        var item1 = AnnotationEdit.forRemoval(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(0, 0), 0);
        var item2 = AnnotationEdit.forRemoval(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(0, 0), 1);
        var item3 = AnnotationEdit.forRemoval(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(0, 0), 2);

        editor.recordAndApply(item1, manager);
        editor.recordAndApply(item2, manager);
        editor.recordAndApply(item3, manager);

        Assertions.assertEquals(3, editor.getCurrentHistorySize());
        Assertions.assertEquals(item1, editor.getHistoryItem(0), "Incorrect undo stack state.");
        Assertions.assertEquals(item2, editor.getHistoryItem(1), "Incorrect undo stack state.");
        Assertions.assertEquals(item3, editor.getHistoryItem(2), "Incorrect undo stack state.");
    }

    @Test
    public void testUndo_OneAnnotation_RestoresStateProperly() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(3);

        var node = model.getCurrentNode();
        var annotations = node.getAnnotations();
        Assertions.assertTrue(annotations.isEmpty(), "Unexpected annotations on root node before test.");

        var item1 = AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(0, 0), 0);
        editor.recordAndApply(item1, manager);

        Assertions.assertEquals(1, annotations.size());
        Assertions.assertEquals(new Annotation.Triangle(0, 0), annotations.iterator().next());

        // Method under test
        editor.performUndo(manager);

        // Assert
        Assertions.assertEquals(0, annotations.size());
    }

    @Test
    public void testUndo_MultipleAnnotations_RestoresStateProperly() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(3);

        // Same session, should be merged into one
        var item1 = AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(0, 0), 0);
        var item2 = AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(1, 0), 0);
        var item3 = AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(2, 0), 0);

        editor.recordAndApply(item1, manager);
        editor.recordAndApply(item2, manager);
        editor.recordAndApply(item3, manager);

        Assertions.assertEquals(1, editor.getCurrentHistorySize(), "Incorrect history size.");
        Assertions.assertEquals(item1, editor.getHistoryItem(0), "Incorrect undo stack state.");
        Assertions.assertTrue(editor.canUndo(), "Cannot undo after merging three annotations edits into one");

        // Method under test
        editor.performUndo(manager);

        // Assert
        Assertions.assertTrue(model.getAnnotationsOnCurrentNode().isEmpty(), "Annotations are not cleared after performing undo.");
    }

    @Test
    public void testConsecutiveUndo_OneAnnotationAddition_RestoresStateProperly() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(3);

        // Same session, should be merged into one
        var item1 = AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(0, 0), 0);
        var item2 = AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(1, 0), 1);
        var item3 = AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(2, 0), 2);

        editor.recordAndApply(item1, manager);
        editor.recordAndApply(item2, manager);
        editor.recordAndApply(item3, manager);

        Assertions.assertEquals(3, editor.getCurrentHistorySize(), "Incorrect initial history size.");
        Assertions.assertEquals(item1, editor.getHistoryItem(0), "Incorrect initial undo stack state.");
        Assertions.assertEquals(item2, editor.getHistoryItem(1), "Incorrect initial undo stack state.");
        Assertions.assertEquals(item3, editor.getHistoryItem(2), "Incorrect initial undo stack state.");
        Assertions.assertTrue(editor.canUndo(), "Incorrect initial canUndo() state.");

        // Method under test (more below)
        editor.performUndo(manager);

        // Assert
        var node = model.getCurrentNode();
        var nodeAnnotations = node.getAnnotations();

        // Use the annotation equality properties to our advantage
        var expectedAnnotations = new HashSet<Annotation>();
        expectedAnnotations.add(new Annotation.Triangle(0, 0));
        expectedAnnotations.add(new Annotation.Triangle(1, 0));
        Assertions.assertTrue(nodeAnnotations.containsAll(expectedAnnotations), "Incorrect annotation state after undo step 1.");

        editor.performUndo(manager);
        expectedAnnotations = new HashSet<>();
        expectedAnnotations.add(new Annotation.Triangle(0, 0));
        Assertions.assertTrue(nodeAnnotations.containsAll(expectedAnnotations), "Incorrect annotation state after undo step 2.");

        editor.performUndo(manager);
        Assertions.assertTrue(nodeAnnotations.isEmpty(), "Annotations are not cleared after final undo.");
    }

    @Test
    public void testConsecutiveUndoRedo_MultipleAnnotationAdditionAndDeletion_RestoresStateProperly() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(3);

        Runnable assertThirdEditState = () -> {
            var annotations = model.getAnnotationsOnCurrentNode();

            Assertions.assertEquals(9, annotations.size());

            for (int i = 0; i < 9; ++i) {
                int x = i % 3;
                int y = i / 3;
                Assertions.assertTrue(annotations.contains(new Annotation.Circle(x, y)));
            }
        };

        Runnable assertSecondEditState = () -> {
            var annotations = model.getAnnotationsOnCurrentNode();

            Assertions.assertEquals(4, annotations.size());
            Assertions.assertTrue(annotations.contains(new Annotation.Triangle(0, 0)));
            Assertions.assertTrue(annotations.contains(new Annotation.Triangle(2, 0)));
            Assertions.assertTrue(annotations.contains(new Annotation.Triangle(1, 1)));
            Assertions.assertTrue(annotations.contains(new Annotation.Triangle(1, 2)));
        };

        Runnable assertFirstEditState = () -> {
            // TODO
        };


        // First edit adds 3 annotations
        // xxx
        // ...
        // ...
        var edits = new Stack<AnnotationEdit>();
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(0, 0), 0));
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(1, 0), 0));
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(2, 0), 0));
        edits.forEach(edit -> editor.recordAndApply(edit, manager));
        assertFirstEditState.run();

        // Second edit deletes 1 annotation and adds 2
        // x.x
        // .x.
        // .x.
        edits.clear();
        edits.push(AnnotationEdit.forRemoval(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(1, 0), 1));
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(1, 1), 1));
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(1, 2), 1));
        edits.forEach(edit -> editor.recordAndApply(edit, manager));
        assertSecondEditState.run();

        // Third edit overlays all annotations with another type
        // ooo
        // ooo
        // ooo
        edits.clear();
        edits.push(AnnotationEdit.forRemoval(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(1, 1), 2));
        edits.push(AnnotationEdit.forRemoval(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(1, 2), 2));
        edits.push(AnnotationEdit.forRemoval(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(0, 0), 2));
        edits.push(AnnotationEdit.forRemoval(manager.getGameModel().getCurrentNode(), new Annotation.Triangle(2, 0), 2));

        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Circle(0, 0), 2));
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Circle(1, 0), 2));
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Circle(2, 0), 2));
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Circle(0, 1), 2));
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Circle(1, 1), 2));
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Circle(2, 1), 2));
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Circle(0, 2), 2));
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Circle(1, 2), 2));
        edits.push(AnnotationEdit.forNew(manager.getGameModel().getCurrentNode(), new Annotation.Circle(2, 2), 2));
        edits.forEach(edit -> editor.recordAndApply(edit, manager));
        assertThirdEditState.run();


        Assertions.assertEquals(3, editor.getCurrentHistorySize()); // 3 different sessions

        // Assertions to be used

        // Check initial annotations are correct
        assertThirdEditState.run();

        // Method under test
        Assertions.assertTrue(editor.canUndo());
        editor.performUndo(manager);
        assertSecondEditState.run();

        Assertions.assertTrue(editor.canUndo());
        editor.performUndo(manager);
        assertFirstEditState.run();

        Assertions.assertTrue(editor.canUndo());
        editor.performUndo(manager);
        Assertions.assertTrue(model.getAnnotationsOnCurrentNode().isEmpty());

        Assertions.assertFalse(editor.canUndo());

        Assertions.assertTrue(editor.canRedo());
        editor.performRedo(manager);
        assertFirstEditState.run();

        Assertions.assertTrue(editor.canRedo());
        editor.performRedo(manager);
        assertSecondEditState.run();

        Assertions.assertTrue(editor.canRedo());
        editor.performRedo(manager);
        assertThirdEditState.run();

        Assertions.assertFalse(editor.canRedo());
    }
}
