package yi.component.boardviewer.editmodes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yi.component.boardviewer.GameBoardClassFactory;
import yi.component.boardviewer.GameBoardManagerAccessor;
import yi.component.boardviewer.edits.PlayMoveEdit;
import yi.component.boardviewer.edits.PlayMoveEditAccessor;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
import yi.core.go.StandardGameRules;
import yi.core.go.StoneColor;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

public final class PlayMoveEditModeUndoRedoTest {

    @Test
    public void testPlayMoveCreatesHistoryItem() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(3);

        // Method under test
        var item1 = PlayMoveEdit.forMove(0, 0);
        editor.recordAndApply(item1, manager);

        Assertions.assertEquals(1, editor.getCurrentHistorySize());
        Assertions.assertEquals(item1, editor.getHistoryItem(0), "Incorrect undo stack state. item2 is not the bottom-most item.");
    }

    @Test
    public void testUndoAdjustsCurrentMove() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(3);

        var item1 = PlayMoveEdit.forMove(0, 0);
        editor.recordAndApply(item1, manager);

        // Method under test
        Assertions.assertTrue(editor.canUndo(), "Cannot undo after playing one move.");
        editor.performUndo(manager);

        Assertions.assertEquals(model.getRootNode(), model.getCurrentNode(), "Current move not adjusted to root node after undoing first move.");
    }

    @Test
    public void testUndoRevertsModelStateCorrectly() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);

        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(9);

        editor.recordAndApply(PlayMoveEdit.forMove(0, 0), manager);
        editor.recordAndApply(PlayMoveEdit.forMove(1, 0), manager);
        editor.recordAndApply(PlayMoveEdit.forMove(2, 0), manager);
        editor.recordAndApply(PlayMoveEdit.forMove(0, 1), manager);
        editor.recordAndApply(PlayMoveEdit.forMove(1, 1), manager);
        editor.recordAndApply(PlayMoveEdit.forMove(2, 1), manager);
        editor.recordAndApply(PlayMoveEdit.forMove(0, 2), manager);
        editor.recordAndApply(PlayMoveEdit.forMove(1, 2), manager);

        var initialBoardPosition = new String[] { " o ",
                                                  "o o",
                                                  " o " };

        // Test setup is correct
        Assertions.assertArrayEquals(initialBoardPosition, getBoardPosition(model), "Initial board position mismatch");
        Assertions.assertTrue(editor.canUndo(), "Cannot begin undo");

        String[][] expectedPositions = new String[][] {
            new String[] {
                " o ",
                "oxo",
                "x  ",
            },

            new String[] {
                " o ",
                "oxo",
                "   ",
            },

            new String[] {
                " ox",
                "ox ",
                "   "
            },

            new String[] {
                " ox",
                "o  ",
                "   "
            },

            new String[] {
                "xox",
                "   ",
                "   "
            },

            new String[] {
                "xo ",
                "   ",
                "   "
            },

            new String[] {
                "x  ",
                "   ",
                "   "
            },

            new String[] {
                "   ",
                "   ",
                "   "
            },
        };

        // Begin test
        for (int step = 0; step < expectedPositions.length; ++step) {
            editor.performUndo(manager);

            Assertions.assertArrayEquals(expectedPositions[step], getBoardPosition(model), "Board position mismatch after undo once.");
            Assertions.assertEquals(8 - step, model.getCurrentNode().getMoveHistory().size(), "Model node length differs from undo steps taken. " +
                    "Error occurred at step " + step + ". The tree structure is not in sync with undo stack.");

            if (step <= initialBoardPosition.length - 1) {
                Assertions.assertTrue(editor.canUndo(),
                        "Cannot undo but not at the bottom of undo stack! Current position: " + editor.getCurrentPositionInHistory() + ", " +
                                "stack size: " + editor.getCurrentHistorySize() + ". Steps: " + step);
            }
        }

        Assertions.assertFalse(editor.canUndo(),
                "Undo still enabled but we are already at the bottom of undo stack! " +
                        "Current position: " + editor.getCurrentPositionInHistory() + ", " +
                        "stack size: " + editor.getCurrentHistorySize());

        Assertions.assertEquals(0, model.getCurrentMoveNumber(), "Not at root node after performing all undo steps.");
    }

    @Test
    public void testUndoReceivesCurrentNodeChangeEvent() throws InterruptedException {
        var newNodeFromEvent = new AtomicReference<GameNode>(null);

        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        model.onCurrentNodeChange().addListener((event) -> newNodeFromEvent.set(event.getNode()));

        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(3);

        var item1 = PlayMoveEdit.forMove(0, 0);
        editor.recordAndApply(item1, manager);

        // Method under test
        Assertions.assertTrue(editor.canUndo(), "Cannot undo after playing one move.");
        editor.performUndo(manager);

        Thread.sleep(10);

        Assertions.assertNotNull(newNodeFromEvent.get(), "Current node change event not received after performing undo");
        Assertions.assertEquals(model.getRootNode(), newNodeFromEvent.get(), "Current node change event did not pass the correct new node after undo");
    }

    @Test
    public void testRedoAdjustsCurrentMove() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(3);

        var item1 = PlayMoveEdit.forMove(0, 0);
        editor.recordAndApply(item1, manager);
        Assertions.assertEquals(model.getRootNode().getNextNodeInMainBranch(), model.getCurrentNode(), "Current move not at root after undo from one move tree.");

        editor.performUndo(manager);
        Assertions.assertEquals(model.getRootNode(), model.getCurrentNode(), "Current move not at root after undo from one move tree.");

        // Method under test
        editor.performRedo(manager);
        Assertions.assertEquals(model.getRootNode().getNextNodeInMainBranch(), model.getCurrentNode(), "Current move not adjusted to root node after performing redo.");
    }

    @Test
    public void testUndoThenRedo_WithManyBranches_ReconstructsModelStateCorrectly() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editor = GameBoardClassFactory.createGameModelEditor();
        editor.setMaxHistorySize(50);

        var branchMain = PlayMoveEdit.forMove(2, 0);

        editor.recordAndApply(PlayMoveEdit.forMove(0, 0), manager);
        editor.recordAndApply(PlayMoveEdit.forMove(1, 0), manager); // <- Create branches from here
        editor.recordAndApply(branchMain, manager);

        // Make some branches of size 1 at the 2nd move
        var branch1 = PlayMoveEdit.forMove(0, 2);
        var branch2 = PlayMoveEdit.forMove(2, 1);
        var branch3 = PlayMoveEdit.forMove(2, 2);
        var branch4 = PlayMoveEdit.forMove(1, 1);
        var branch5 = PlayMoveEdit.forMove(1, 2);
        var branch6 = PlayMoveEdit.forMove(0, 1);

        model.toPreviousNode();
        editor.recordAndApply(branch1, manager);
        model.toPreviousNode();
        editor.recordAndApply(branch2, manager);
        model.toPreviousNode();
        editor.recordAndApply(branch3, manager);
        model.toPreviousNode();
        editor.recordAndApply(branch4, manager);
        model.toPreviousNode();
        editor.recordAndApply(branch5, manager);
        model.toPreviousNode();
        editor.recordAndApply(branch6, manager);
        model.toPreviousNode(); // Restore back to branching point

        var branchesIncludingMain = model.getCurrentNode().getNextNodes();
        Assertions.assertEquals(7, branchesIncludingMain.size());

        // The branches we expect to remain on the tree after each undo
        var branchStack = new Stack<GameNode>();
        branchStack.push(PlayMoveEditAccessor.getGameNode(branchMain).orElseThrow());
        branchStack.push(PlayMoveEditAccessor.getGameNode(branch1).orElseThrow());
        branchStack.push(PlayMoveEditAccessor.getGameNode(branch2).orElseThrow());
        branchStack.push(PlayMoveEditAccessor.getGameNode(branch3).orElseThrow());
        branchStack.push(PlayMoveEditAccessor.getGameNode(branch4).orElseThrow());
        branchStack.push(PlayMoveEditAccessor.getGameNode(branch5).orElseThrow());
        branchStack.push(PlayMoveEditAccessor.getGameNode(branch6).orElseThrow());

        // The branches popped off the undo history due to redo, that we expect to be restored in this order
        // in the subsequent redo.
        var poppedStack = new Stack<GameNode>();

        // Undo it
        for (int i = 0; i < 6; ++i) {
            Assertions.assertTrue(editor.canUndo(), "Cannot undo anymore after " + i + " undo step(s)");
            editor.performUndo(manager);
            poppedStack.push(branchStack.pop());

            // Check the remaining branches are in the identical order as given by branchStack
            for (int childPosition = 0; childPosition < branchesIncludingMain.size(); ++childPosition) {
                var expectedNodeInThisPosition = branchStack.get(childPosition);
                Assertions.assertEquals(expectedNodeInThisPosition, branchesIncludingMain.get(childPosition),
                        "Child nodes (branch) order is incorrect after performing " + i + " steps of undo.");
            }
        }

        // Redo it
        for (int i = 0; i < 6; ++i) {
            Assertions.assertTrue(editor.canUndo(), "Cannot redo anymore after " + i + " undo step(s)");
            editor.performRedo(manager);
            branchStack.push(poppedStack.pop());

            Assertions.assertEquals(2 + i, branchesIncludingMain.size(), "Branch size unexpected");
            // Check the remaining branches are in the identical order as given by branchStack
            for (int childPosition = 0; childPosition < branchesIncludingMain.size(); ++childPosition) {
                var expectedNodeInThisPosition = branchStack.get(childPosition);
                Assertions.assertEquals(expectedNodeInThisPosition, branchesIncludingMain.get(childPosition),
                        "Child nodes (branch) order is incorrect after performing " + i + " steps of redo.");
            }
        }
    }

    // TODO: Add these once pass and resign edits are properly implemented.
//    @Test
//    public void testUndo_Pass_StateCorrect() {
//        var model = new GameModel(3, 3, GameRules.CHINESE);
//        var manager = GameBoardClassFactory.createGameBoardManager();
//        GameBoardManagerAccessor.setGameModel(manager, model);
//
//        var editor = GameBoardClassFactory.createGameModelEditor();
//        editor.setMaxHistorySize(50);
//
//        editor.recordAndApply(PlayMoveEdit.forPass(), manager);
//        Assertions.assertEquals(1, editor.getCurrentHistorySize());
//    }
//
//    @Test
//    public void testRedo_Pass_StateCorrect() {
//        // TODO: Implement me
//    }
//
//    @Test
//    public void testUndo_Resign_StateCorrect() {
//        // TODO: Implement me
//    }
//
//    @Test
//    public void testRedo_Resign_StateCorrect() {
//        // TODO: Implement me
//    }

    private String[] getBoardPosition(GameModel model) {
        var state = model.getCurrentGameState();
        var position = state.getBoardPosition();

        String[] positionData = new String[model.getBoardHeight()];
        for (int y = 0; y < positionData.length; ++y) {
            StringBuilder rowData = new StringBuilder();
            for (int x = 0; x < model.getBoardWidth(); ++x) {
                var stone = position.getStoneColorAt(x, y);
                if (stone == StoneColor.BLACK)
                    rowData.append("x");
                else if (stone == StoneColor.WHITE)
                    rowData.append("o");
                else
                    rowData.append(" ");
            }
            positionData[y] = rowData.toString();
        }
        return positionData;
    }
}
