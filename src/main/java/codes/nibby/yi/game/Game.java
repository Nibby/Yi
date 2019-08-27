package codes.nibby.yi.game;

import codes.nibby.yi.board.Stone;
import codes.nibby.yi.game.rules.IGameRules;
import jdk.jshell.spi.ExecutionControl;

import java.util.*;

/**
 * The core representation for a Go 'game'. This class contains all the
 * information to generate a game record file.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class Game {

    public static final int COLOR_BLACK = 1;
    public static final int COLOR_WHITE = 2;

    /** Width of the go board. */
    private int boardWidth;

    /** Height of the go board. */
    private int boardHeight;

    /** The root of the game tree. */
    private GameNode gameTree;

    /** The current selected node in the game tree. */
    private GameNode currentNode;

    /** Current rules governing the game. */
    private IGameRules ruleset;

    /**
     * A list of the game positions leading up to currentNode.
     * This is used to prevent repeating global positions.
     */
    private Map<Integer, GameNode> pastStates;

    /** Listeners for the game. */
    private List<GameListener> listeners = new ArrayList<>();

    public Game(IGameRules rules, int boardWidth, int boardHeight) {
        setRuleset(rules);
        setBoardSize(boardWidth, boardHeight);
    }

    /**
     * Resets the game state. All nodes will be deleted.
     */
    private void initialize() {
        gameTree = new GameNode();
        int[] boardData = new int[boardWidth * boardHeight];
        gameTree.setStoneData(boardData);
        pastStates = new HashMap<>();
        constructBoardState(null, gameTree);
        currentNode = gameTree;

        for (GameListener l : listeners)
            l.gameInitialized(this);

    }

    /**
     * Checks whether a move can be played at (x, y), if yes, the move is played,
     * otherwise it is aborted.
     *
     * The color of the stone is determined by the result of <i>getNextMoveColor();</i>
     *
     * @param x X position on the board.
     * @param y Y position on the board.
     * @return The result of the proposal.
     */
    public ProposalResult proposeMove(int x, int y) {
        return ruleset.proposeAndSubmitMove(this, getNextMoveColor(), x, y);
    }

    /**
     * Updates the game to the a new node position.
     * TODO: The new node must be part of the game tree.
     *
     * @param newNode The new currentNode
     * @param isNewMove Whether this node is the newest move played on the board.
     */
    public void setCurrentNode(GameNode newNode, boolean isNewMove) {
        constructBoardState(currentNode, newNode);
        this.currentNode = newNode;

        for (GameListener l : listeners)
            l.gameCurrentMoveUpdate(newNode, isNewMove);
    }

    /**
     * Constructs pastState from current node.
     *
     * This method takes an oldNode as reference. If oldNode is 1 step ahead
     * or behind the newNode then the algorithm won't reinitialise the entire
     * data structure.
     *
     * @param oldNode The former 'currentNode'
     * @param newNode The latest 'currentNode'
     */
    public void constructBoardState(GameNode oldNode, GameNode newNode) {
        // If there is no old nodes, then newNode must be root node.
        if (oldNode == null) {
            pastStates.put(0, newNode);
            return;
        }

        // Otherwise, adjust the state map accordingly...
        int step = oldNode.getMoveNumber() - newNode.getMoveNumber();

        // The new move is 1 move ahead of the old
        // In this case we append the new move to the current state.
        if (step == 1) {
            pastStates.put(newNode.getMoveNumber(), newNode);
        }
        // If the new move is 1 step behind the old,
        // remove the oldNode from state map.
        else if (step == -1) {
            pastStates.remove(oldNode.getMoveNumber());
        }
        // Otherwise, the new node is further from old node in terms of
        // move number. The state map is reconstructed.
        else {
            pastStates.clear();
            GameNode[] chain = newNode.getNodeHistory();
            for (int move = 0; move < chain.length; move++) {
                pastStates.put(move, chain[move]);
            }
        }
    }

    /**
     * Sets the game rules.
     *
     * @param rules Game rules to use.
     */
    public void setRuleset(IGameRules rules) {
        this.ruleset = rules;
    }

    /**
     * Performs scoring on the position as given by <i>currentNode</i> and
     * returns the outcome of the game.
     *
     * TODO: Implement this later
     * @return Information regarding the outcome of the game.
     * @throws ExecutionControl.NotImplementedException Not implemented!
     */
    public GameOutcome getOutcome() throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("Not yet implemented!");
    }

    /**
     * Returns the number code of the next player to move.
     * TODO: Integrate this with game rules
     *
     * @return COLOR_BLACK or COLOR_WHITE, depending on oddity of the move number.
     */
    public int getNextMoveColor() {
        return currentNode.getMoveNumber() % 2 == 1 ? COLOR_WHITE : COLOR_BLACK;
    }

    /**
     * A destructive operation that resizes the go board.
     * Will reset game tree.
     *
     * @param width New board width.
     * @param height New board height.
     */
    public void setBoardSize(int width, int height) {
        this.boardWidth = width;
        this.boardHeight = height;

        initialize();
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public GameNode getGameTree() {
        return gameTree;
    }

    public GameNode getCurrentNode() {
        return currentNode;
    }

    public void addGameListener(GameListener listener) {
        listeners.add(listener);
    }

    public IGameRules getRuleset() {
        return ruleset;
    }

    public GameNode createNextNode() {
        GameNode nextNode = new GameNode(currentNode);
        nextNode.setColor(getNextMoveColor());
        return nextNode;
    }
}
