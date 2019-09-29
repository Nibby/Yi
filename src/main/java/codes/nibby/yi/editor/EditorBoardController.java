package codes.nibby.yi.editor;

import codes.nibby.yi.board.GameBoard;
import codes.nibby.yi.board.GameBoardController;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameNode;
import codes.nibby.yi.game.rules.ProposalResult;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * The board controller used by the GameRecordEditor window.
 *
 * @author Kevin Yang
 * Created on 27 August 2019
 */
public class EditorBoardController extends GameBoardController {

    @Override
    public void gameInitialized(Game game) {

    }

    @Override
    public void gameCurrentMoveUpdate(GameNode currentMove, boolean newMove) {

    }

    @Override
    public void initialize(Game game, GameBoard board) {
        super.initialize(game, board);

    }

    @Override
    public void mouseMoved(int x, int y, int oldX, int oldY) {
        super.mouseMoved(x, y, oldX, oldY);
    }

    @Override
    public void mousePressed(int x, int y, int oldX, int oldY, MouseButton button) {
        super.mousePressed(x, y, oldX, oldY, button);

        if (button.equals(MouseButton.PRIMARY)) {
            ProposalResult proposal = getGame().proposeMove(x, y);
//            System.out.println("Proposal result : " + proposal.getType().name());
            boolean successful = getGame().submitMove(proposal);
        }
    }

    @Override
    public void mouseReleased(int x, int y, int oldX, int oldY, MouseButton button) {
        super.mouseReleased(x, y, oldX, oldY, button);
    }

    @Override
    public void mouseDragged(int x, int y, int oldX, int oldY, MouseButton button) {
        super.mouseDragged(x, y, oldX, oldY, button);
    }

    @Override
    public void mouseScrolled(double notch) {
        super.mouseScrolled(notch);
        if (notch > 0) {
            GameNode node = getGame().getCurrentNode();
            if (node.hasParent())
                node = node.getParent();
            getGame().setCurrentNode(node, false);
        } else if (notch < 0) {
            GameNode node = getGame().getCurrentNode();
            if (node.hasChildren())
                node = node.getChildren().get(0);
            getGame().setCurrentNode(node, false);
        }
    }

    @Override
    public void mouseEntered() {
        super.mouseEntered();
    }

    @Override
    public void mouseExited() {
        super.mouseExited();
    }

    @Override
    public void keyPressed(KeyCode code) {
        super.keyPressed(code);
    }

    @Override
    public void keyReleased(KeyCode code) {
        super.keyReleased(code);
    }
}
