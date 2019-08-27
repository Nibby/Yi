package codes.nibby.yi.game.rules;

import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameNode;
import codes.nibby.yi.game.ProposalResult;

import java.util.Arrays;

/**
 * Base class that represents all <strong>conventional</strong> Go rules.
 *
 * @author Kevin Yang
 * Created on
 */
public abstract class StandardGameRules implements IGameRules {

    @Override
    public ProposalResult proposeAndSubmitMove(Game game, int color, int x, int y) {
        // TODO: TEMPORARY! Add rules later
        GameNode newNode = game.createNextNode();
        int expectedColor = game.getNextMoveColor();
        if (color != expectedColor)
            return ProposalResult.INVALID_ERR_COLOR_MISMATCH;

        // Update game node data.
        GameNode currentNode = game.getCurrentNode();
        int[] currentData = currentNode.getStoneData();
        int[] newData = Arrays.copyOf(currentData, currentData.length);
        newData[y * game.getBoardWidth() + x] = color;
        newNode.setStoneData(newData);
        newNode.setCurrentMove(new int[] { x, y });
        currentNode.addChild(newNode);
        game.setCurrentNode(newNode, true);
        return ProposalResult.SUCCESS;
    }

}
