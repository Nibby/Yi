package yi.component.gametree;

import org.junit.jupiter.api.Test;
import yi.core.common.GameNode;
import yi.core.go.GoGameModel;
import yi.core.go.GoGameStateUpdate;
import yi.core.go.rules.GoGameRulesHandler;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TreeStructureCorrectnessTest {

    private static final class TestingRules extends GoGameRulesHandler {

        @Override
        public float getKomi() {
            return 0;
        }

        @Override
        public boolean allowSuicideMoves() {
            return false;
        }
    }


    @Test
    public void testLinearSequence() {
        var model = new GoGameModel(3, 3, new TestingRules());
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(0, 1)
                .playMove(0, 2);

        var structure = new GameTreeStructure(model);

        var expectedStructure = new String[] {
                "x",
                "x",
                "x",
                "x"
        };
        testTreeStructure(model, structure, expectedStructure);
    }

    @Test
    public void testWithOneSideBranch() {
        var model = new GoGameModel(3, 3, new TestingRules());
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(0, 1);

        model.toPreviousMove(); // <- Goes back to (0, 0) aka first move

        model.beginMoveSequence()
                .playMove(0, 2)
                .playMove(0, 1)
                .playMove(1, 0);

        var structure = new GameTreeStructure(model);

        // Expected shape:
        // [x]
        //  |
        // [x]-----|
        //  |      |
        // [x]    [x]
        //         |
        //        [x]
        //         |
        //        [x]

        String[] expectedStructure = {
                "x ",
                "x ",
                "xx",
                " x",
                " x"
        };

        testTreeStructure(model, structure, expectedStructure);
    }

    @Test
    public void testWithThreeSideBranch() {
        var model = new GoGameModel(3, 3, new TestingRules());
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(0, 1);

        model.toPreviousMove(); // <- Goes back to (0, 0) aka first move

        model.beginMoveSequence()
                .playMove(0, 2);

        model.toPreviousMove();

        model.beginMoveSequence()
                .playMove(1, 2);

        model.toPreviousMove();

        model.beginMoveSequence()
                .playMove(2, 2);


        String[] expectedStructure = {
                "x   ",
                "x   ",
                "xxxx",
        };

        testTreeStructure(model, new GameTreeStructure(model), expectedStructure);
    }

    @Test
    public void testBranches_createNewVariationDownstream_PushesParentVariationsOutwards() {
        var model = new GoGameModel(3, 3, new TestingRules());

        // Setup test
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(0, 1)
                .playMove(0, 2)
                .playMove(1, 0)
                .playMove(1 ,1);

        model.toPreviousMove(3);
        model.beginMoveSequence().playMove(2, 2).playMove(2, 1).playMove(2, 0);
        model.toPreviousMove(3);
        model.beginMoveSequence().playMove(1, 2).playMove(1, 0).playMove(0, 2);
        model.toPreviousMove(3);
        model.toNextMove(2);

        String[] expectedInitialStructure = {
                "x  ",
                "x  ",
                "x  ",
                "xxx",
                "xxx",
                "xxx",
        };
        testTreeStructure(model, new GameTreeStructure(model), expectedInitialStructure); // Sanity check

        // Begin test
        model.beginMoveSequence().playMove(2, 1);
        String[] expectedStructure = {
                "x   ",
                "x   ",
                "x   ",
                "x xx",
                "x xx",
                "xxxx",
        };
        testTreeStructure(model, new GameTreeStructure(model), expectedStructure);
    }

    @Test
    public void testColumnAdjust_LongTrackBelow_AncestorVariationDoesNotIntersect() {
        var model = new GoGameModel(3, 3, new TestingRules());

        // Build main branch
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(0, 1)
                .playMove(0, 2)
                .playMove(1, 0)
                .playMove(1 ,1);

        model.toPreviousMove();
        model.beginMoveSequence().playMove(2, 2);
        model.toPreviousMove();
        model.beginMoveSequence().playMove(2, 1);
        model.toPreviousMove();
        model.beginMoveSequence().playMove(2, 0);

        // Create a wide horizontal track
        model.toPreviousMove(2);
        model.beginMoveSequence().playMove(1, 1);

        model.toPreviousMove(3);
        String[] expectedInitialStructure = {
                "x    ",
                "x    ",
                "x    ",
                "x    ",
                "x   x",
                "xxxx ",
        };
        testTreeStructure(model, new GameTreeStructure(model), expectedInitialStructure); // Sanity check

        // Begin test
        model.beginMoveSequence().playMove(0, 2);
        String[] expectedStructure = {
                "x    ",
                "x    ",
                "xx   ",
                "x    ",
                "x   x",
                "xxxx ",
        };
        testTreeStructure(model, new GameTreeStructure(model), expectedStructure);

        model.beginMoveSequence().playMove(0, 1);
        // Upon playing the move above, the last node of this variation will hit the track of a branch down below
        // In which case this branch should occupy the right-most column.
        expectedStructure = new String[] {
                "x     ",
                "x     ",
                "x    x",
                "x    x",
                "x   x ",
                "xxxx  ",
        };
        testTreeStructure(model, new GameTreeStructure(model), expectedStructure);
    }

    @Test
    public void testColumnAdjust_LongAncestorBranches_FindsNewColumnOnRight() {
        var model = new GoGameModel(3, 3, new TestingRules());

        // Build main branch
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(0, 1)
                .playMove(0, 2)
                .playMove(1, 0)
                .playMove(1 ,1);

        // Build side branch of length 1
        model.toPreviousMove(3);
        model.beginMoveSequence().playMove(2, 2);

        // Build two side branches of length 1
        model.toPreviousMove();
        model.toNextMove(2);
        model.beginMoveSequence().playMove(2, 1);
        model.toPreviousMove();
        model.beginMoveSequence().playMove(1, 2);
        model.toPreviousMove(5); // Resets back to root

        String[] expectedInitialStructure = {
                "x  ",
                "x  ",
                "x  ",
                "xx ",
                "x  ",
                "xxx",
        };
        testTreeStructure(model, new GameTreeStructure(model), expectedInitialStructure); // Sanity check

        // Current setup:
        //  [x] ===[|?] <- Expand a branch here that is 6 nodes long, it should cascade down the columns and expand to the right when necessary
        //   |
        //  [x]
        //   |
        //  [x]---|
        //   |    |
        //  [x]  [x]
        //   |
        //  [x]---|---|
        //   |    |   |
        //  [x]  [x] [x]

        model.beginMoveSequence().playMove(2, 2);

        String[] expectedStructure = {
                "x  ",
                "xx ",
                "x  ",
                "xx ",
                "x  ",
                "xxx",
        };
        testTreeStructure(model, new GameTreeStructure(model), expectedStructure);

        model.beginMoveSequence().playMove(2, 1);
        expectedStructure = new String[] {
                "x  ",
                "x x",
                "x x",
                "xx ",
                "x  ",
                "xxx"
        };
        testTreeStructure(model, new GameTreeStructure(model), expectedStructure);

        model.beginMoveSequence().playMove(2, 0);
        expectedStructure = new String[] {
                "x  ",
                "x x",
                "x x",
                "xxx",
                "x  ",
                "xxx"
        };
        testTreeStructure(model, new GameTreeStructure(model), expectedStructure);

        model.beginMoveSequence().playMove(1, 2);
        expectedStructure = new String[] {
                "x   ",
                "x  x",
                "x  x",
                "xx x",
                "x  x",
                "xxx "
        };
        testTreeStructure(model, new GameTreeStructure(model), expectedStructure);

        model.beginMoveSequence().playMove(0, 2);
        expectedStructure = new String[] {
                "x   ",
                "x  x",
                "x  x",
                "xx x",
                "x  x",
                "xxxx"
        };
        testTreeStructure(model, new GameTreeStructure(model), expectedStructure);
    }

    private void testTreeStructure(GoGameModel model, GameTreeStructure structure, String[] expectedStructure) {
        boolean[][] grids = generateExpectedGridSpace(expectedStructure);
        boolean[][] actualGrid = new boolean[grids.length][grids[0].length];

        // Probe the entire structure to check if it complies
        for (int x = 0; x < grids.length; ++x) {
            for (int y = 0; y < grids[0].length; ++y) {
                var elementHere = structure.getElement(x, y);
                boolean hasNodeHere = elementHere.isPresent() && elementHere.get() instanceof TreeNodeElement;

                actualGrid[x][y] = hasNodeHere;
            }
        }

        // Convert to string for comparison so that if it fails, we get a better formatted output
        // The actual structure is limited by expected structure dimensions here...
        String[] actualStructure = new String[expectedStructure.length];
        for (int row = 0; row < expectedStructure.length; ++row) {
            StringBuilder rowData = new StringBuilder();
            for (int column = 0; column < expectedStructure[row].length(); ++column) {
                boolean hasNode = actualGrid[column][row];

                rowData.append(hasNode ? "x" : " ");
            }
            actualStructure[row] = rowData.toString();
        }

        assertArrayEquals(expectedStructure, actualStructure, "Actual structure may be incomplete as only the space defined by the expected structure is checked.");

        // Check the branch nodes in detail
        testBranch(model.getCurrentMove().getRoot(), structure, grids);
    }

    private void testBranch(GameNode<GoGameStateUpdate> node, GameTreeStructure structure, boolean[][] grids) {
        var currentNode = node;
        Collection<TreeElement> allElements = structure.getElements();

        while (currentNode != null) {
            // 1. Test whether an element is there
            // 2. Test element-to-node correspondence
            GameNode<GoGameStateUpdate> finalCurrentNode = currentNode;
            Optional<TreeElement> elementOfNode = allElements.parallelStream()
                    .filter(element -> element instanceof TreeNodeElement && ((TreeNodeElement) element).getNode().equals(finalCurrentNode))
                    .findAny();

            assertTrue(elementOfNode.isPresent(), "Cannot find corresponding tree element for node '" + currentNode.toString() + "'");

            var element = elementOfNode.get();
            int elementX = element.getLogicalX();
            int elementY = element.getLogicalY();

            // 3. Test logical coordinate correct
            String errorMessage = String.format("Unexpected node at %d,%d : '%s'", elementX, elementY, currentNode.toString());
            assertTrue(grids[elementX][elementY], errorMessage);

            var children = currentNode.getChildren();
            if (children.size() > 0) {
                if (children.size() > 1) {
                    for (int i = 1; i < children.size(); ++i) {
                        testBranch(children.get(i), structure, grids);
                    }
                }

                currentNode = children.get(0);
            } else {
                break;
            }
        }
    }

    private boolean[][] generateExpectedGridSpace(String[] expectedStructure) {
        int width = expectedStructure[0].length();
        int height = expectedStructure.length;
        var grids = new boolean[width][height]; // true = presence of an element

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                grids[x][y] = expectedStructure[y].charAt(x) != ' ';
            }
        }

        return grids;
    }
}
