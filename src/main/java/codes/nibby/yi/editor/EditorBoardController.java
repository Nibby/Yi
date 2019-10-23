package codes.nibby.yi.editor;

import codes.nibby.yi.board.GameBoard;
import codes.nibby.yi.board.GameBoardController;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameNode;
import codes.nibby.yi.game.Markup;
import codes.nibby.yi.game.MarkupType;
import codes.nibby.yi.game.rules.ProposalResult;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.List;

/**
 * The board controller used by the GameRecordEditor window.
 *
 * @author Kevin Yang
 * Created on 27 August 2019
 */
public class EditorBoardController extends GameBoardController {

    private static final int MARKUP_MODE_ADD = 0;
    private static final int MARKUP_MODE_REMOVE = 1;

    private EditorToolType toolType = EditorToolType.PLAY_MOVE;
    private int lastMarkupMode;
    private String lastLabelText;

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
        GameNode node = getGame().getCurrentNode();

        switch (toolType) {
            case PLAY_MOVE:
                if (button.equals(MouseButton.PRIMARY)) {
                    // Check if move already exists in the parent
                    GameNode currentNode = getGame().getCurrentNode();
                    List<GameNode> children = currentNode.getChildren();
                    boolean foundMove = false;
                    for (GameNode child : children) {
                        int[] currentMove = child.getCurrentMove();
                        if (currentMove[0] == x && currentMove[1] == y) {
                            getGame().setCurrentNode(child, false);
                            foundMove = true;
                        }
                    }

                    if (!foundMove) {
                        ProposalResult proposal = getGame().proposeMove(x, y);
                        getGame().submitMove(proposal);
                    }
                }
                break;
            case ADD_HELPER_BLACK:
            case ADD_HELPER_WHITE:
                // TODO implement later
                break;
            default:
                if (button.equals(MouseButton.PRIMARY)) {
                    if (!node.hasMarkupAt(x, y, true)) {
                        switch (toolType) {
                            case MARKUP_TRIANGLE:
                                node.addMarkup(Markup.triangle(x, y));
                                break;
                            case MARKUP_CIRCLE:
                                node.addMarkup(Markup.circle(x, y));
                                break;
                            case MARKUP_CROSS:
                                node.addMarkup(Markup.cross(x, y));
                                break;
                            case MARKUP_SQUARE:
                                node.addMarkup(Markup.square(x, y));
                                break;
                            case MARKUP_LABEL_LETTER:
                                lastLabelText = getNextMarkupLetter();
                                node.addMarkup(Markup.label(x, y, lastLabelText));
                                break;
                            case MARKUP_LABEL_NUMBER:
                                lastLabelText = getNextMarkupNumber();
                                node.addMarkup(Markup.label(x, y, lastLabelText));
                                break;
                        }

                        lastMarkupMode = MARKUP_MODE_ADD;
                    } else
                        lastMarkupMode = MARKUP_MODE_REMOVE;
                } else if (button.equals(MouseButton.SECONDARY)) {
                    // Removes any markup at this spot
                    // Though the method name can be very confusing...
                    node.hasMarkupAt(x, y, true);
                    lastMarkupMode = MARKUP_MODE_REMOVE;
                }
                break;
        }
    }

    @Override
    public void mouseReleased(int x, int y, int oldX, int oldY, MouseButton button) {
        super.mouseReleased(x, y, oldX, oldY, button);
    }

    @Override
    public void mouseDragged(int x, int y, int oldX, int oldY, MouseButton button) {
        super.mouseDragged(x, y, oldX, oldY, button);
        GameNode node = getGame().getCurrentNode();

        if (button.equals(MouseButton.PRIMARY)) {
            if (lastMarkupMode == MARKUP_MODE_ADD && !node.hasMarkupAt(x, y, false)) {
                switch (toolType) {
                    case MARKUP_TRIANGLE:
                        node.addMarkup(Markup.triangle(x, y));
                        break;
                    case MARKUP_CIRCLE:
                        node.addMarkup(Markup.circle(x, y));
                        break;
                    case MARKUP_CROSS:
                        node.addMarkup(Markup.cross(x, y));
                        break;
                    case MARKUP_SQUARE:
                        node.addMarkup(Markup.square(x, y));
                        break;
                    case MARKUP_LABEL_LETTER:
                    case MARKUP_LABEL_NUMBER:
                        node.addMarkup(Markup.label(x, y, lastLabelText));
                        break;
                }
            } else if (lastMarkupMode == MARKUP_MODE_REMOVE) {
                node.hasMarkupAt(x, y, true);
            }
        } else if (button.equals(MouseButton.SECONDARY)) {
            // Removes any markup at this spot
            // Though the method name can be very confusing...
            node.hasMarkupAt(x, y, true);
        }
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

    public EditorToolType getToolType() {
        return toolType;
    }

    public void setToolType(EditorToolType toolType) {
        this.toolType = toolType;
    }

    private String getNextMarkupLetter() {
        GameNode node = getGame().getCurrentNode();
        List<Markup> markups = node.getMarkups();
        char c = 'A';
        for (Markup markup : markups) {
            if (markup.getType() == MarkupType.LABEL) {
                if (String.valueOf(c).equals(markup.getArguments()))
                    c += 1;
                if (c > 'Z' && c < 'a') {
                    c = 'a';
                } else if (c > 'z') {
                    c = 'A';
                }
            }
        }

        return String.valueOf(c);
    }

    private String getNextMarkupNumber() {
        GameNode node = getGame().getCurrentNode();
        List<Markup> markups = node.getMarkups();
        int i = 1;
        for (Markup markup : markups) {
            if (markup.getType() == MarkupType.LABEL) {
                if (String.valueOf(i).equals(markup.getArguments()))
                    i ++;
            }
        }

        return String.valueOf(i);
    }

}
