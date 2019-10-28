package codes.nibby.yi.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single game 'state' in the game tree.
 * Each node has 1 parent and 0+ children.
 * <p>
 * Internally the game states are stored as a string of <strong>m x n</strong> integers
 * where <strong>m</strong> is the board width and <strong>n</strong> is the board height.
 * <p>
 * The numbers on the state register represent:
 * <ul>
 *     <li><strong>0</strong>: Nothing</li>
 *     <li><strong>1</strong>: Black stone</li>
 *     <li><strong>2</strong>: White stone</li>
 * </ul>
 * <p>
 * The object for displayed stones on the board are found in <strong>Game</strong> class.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class GameNode {

    /*
        Game node data:
     */
    // Current move that is played
    // This is an array of size 2:
    // move[0] = moveX, move[1] = moveY
    private int[] currentMove;
    // Stone color
    private int color;

    /**
     * List of captures at this node.
     */
    private int prisonersBlack, prisonersWhite;

    /**
     * Move comments
     */
    private String comments = "";

    /**
     * Board labels, markers etc.
     */
    private List<Markup> markups = new ArrayList<>();

    /**
     * A list of black demo stones.
     */
    private List<Integer> helperStonesBlack = new ArrayList<>();

    /**
     * A list of white demo stones.
     */
    private List<Integer> helperStonesWhite = new ArrayList<>();

    /**
     * A list of points on the board where stones are removed.
     */
    private List<Integer> clearPointsList = new ArrayList<>();

    /**
     * The position of the ko square
     */
    private int koPoint = -1;

    // Game tree hierarchy
    /**
     * The parent node
     */
    private GameNode parent = null;

    /**
     * Children nodes, with index 0 being the main variation child.
     */
    private List<GameNode> children = new ArrayList<>();

    /**
     * Board stone positions at this point.
     */
    private int[] stoneData;

    private Game game;
    private boolean pass = false;

    public GameNode(Game game) {
        this(game, null);
    }

    public GameNode(Game game, GameNode parent) {
        this.game = game;
        this.parent = parent;
    }

    public void addChild(GameNode node) {
        addChild(node, false);
    }

    /**
     * Append a child node to this node, with the option of making the child node
     * part of the main branch.
     *
     * @param node          Child node to be appended.
     * @param makeMainChild Whether to make child node part of the main variation.
     */
    public void addChild(GameNode node, boolean makeMainChild) {
        if (makeMainChild)
            children.add(0, node);
        else
            children.add(node);

        game.setModified(true);
    }

    public boolean hasChildren() {
        return children.size() > 0;
    }

    public GameNode getParent() {
        return parent;
    }

    public List<GameNode> getChildren() {
        return children;
    }

    public boolean hasParent() {
        return parent != null;
    }

    /**
     * @return The distance to the root.
     */
    public int getMoveNumber() {
        int steps = 0;
        GameNode current = this;
        while (current.hasParent()) {
            current = current.getParent();
            steps++;
        }
        return steps;
    }

    /**
     * Builds a sequential array of game states that leads up to the current state.
     *
     * @return An array of game states, with root at index 0.
     */
    public GameNode[] getNodeHistory() {
        int size = getMoveNumber() + 1;
        GameNode[] result = new GameNode[size];
        GameNode current = this;
        for (int step = size - 1; step > 0; step--) {
            result[step] = current;
            current = current.getParent();
        }
        return result;
    }

    public int[] getStoneData() {
        return stoneData;
    }

    public void setStoneData(int[] stoneData) {
        this.stoneData = stoneData;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int[] getCurrentMove() {
        return currentMove;
    }

    public void setCurrentMove(int[] currentMove) {
        this.currentMove = currentMove;
    }

    public int getKoPoint() {
        return koPoint;
    }

    public void setKoPoint(int koPoint) {
        this.koPoint = koPoint;
    }

    public int getPrisonersBlack() {
        return prisonersBlack;
    }

    public void setPrisonersBlack(int prisonersBlack) {
        this.prisonersBlack = prisonersBlack;
    }

    public int getPrisonersWhite() {
        return prisonersWhite;
    }

    public void setPrisonersWhite(int prisonersWhite) {
        this.prisonersWhite = prisonersWhite;
    }

    public void addPrisonersBlack(int blackPrisoners) {
        prisonersBlack += blackPrisoners;
    }

    public void addPrisonersWhite(int whitePrisoners) {
        prisonersWhite += whitePrisoners;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isRootNode() {
        return getParent() == null;
    }

    public List<Markup> getMarkups() {
        return markups;
    }

    public void setMarkups(List<Markup> markups) {
        this.markups = markups;
    }

    public void addMarkup(Markup markup) {
        markups.add(markup);
        game.updateNode(this);
        game.setModified(true);
    }

    public boolean hasMarkupAt(int x, int y, boolean shouldRemove) {
        boolean removed = false;
        for (int i = 0; i < markups.size(); ) {
            Markup markup = markups.get(i);
            if (markup.getX1() == x && markup.getY1() == y) {
                if (shouldRemove) {
                    markups.remove(markup);
                    removed = true;
                } else
                    return true;
            } else {
                i++;
            }
        }
        if (shouldRemove && removed) {
            game.updateNode(this);
            game.setModified(true);
        }
        return removed;
    }

    public void toggleHelperStone(int color, int x, int y) {
        int index = x + y * game.getBoardWidth();
        if (index < 0 || index > game.getBoardWidth() * game.getBoardHeight())
            throw new IllegalArgumentException("Helper stone position out of bounds!");
        boolean exists = helperStonesBlack.contains(color) || helperStonesWhite.contains(color);
        boolean updated = false;
        int[] stones = getStoneData();
        if (stones[index] != color) {
            // If a stone exists in this position but has a different color,
            // then turn it into the current color.
            stones[index] = color;
            updated = true;
        } else if (stones[index] == color) {
            // If a stone exists and it's already of the same color
            // then remove it from the board.
            stones[index] = Game.COLOR_NONE;
            updated = true;
        }

        if (updated) {
            game.updateNode(this);
            game.setModified(true);
        }
    }

    public void addHelperStone(int color, int x, int y) {
        int index = x + y * game.getBoardWidth();
        if (index < 0 || index > game.getBoardWidth() * game.getBoardHeight())
            throw new IllegalArgumentException("Helper stone position out of bounds!");

        switch (color) {
            case Game.COLOR_BLACK:
                if (!helperStonesBlack.contains(index))
                    helperStonesBlack.add(index);
                break;
            case Game.COLOR_WHITE:
                if (!helperStonesWhite.contains(index))
                    helperStonesWhite.add(index);
                break;
        }
    }

    public List<Integer> getHelperStonesBlack() {
        return helperStonesBlack;
    }

    public void setHelperStonesBlack(List<Integer> helperStonesBlack) {
        this.helperStonesBlack = helperStonesBlack;
    }

    public List<Integer> getHelperStonesWhite() {
        return helperStonesWhite;
    }

    public void setHelperStonesWhite(List<Integer> helperStonesWhite) {
        this.helperStonesWhite = helperStonesWhite;
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean flag) {
        pass = true;
    }

    public List<Integer> getClearPoints() {
        return clearPointsList;
    }
}
