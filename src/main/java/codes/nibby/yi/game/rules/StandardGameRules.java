package codes.nibby.yi.game.rules;

import codes.nibby.yi.board.Stone;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Base class that represents all <strong>conventional</strong> Go rules.
 *
 * @author Kevin Yang
 * Created on 28 August 2019
 */
public abstract class StandardGameRules implements IGameRules {

    @Override
    public ProposalResult proposeMove(Game game, int color, int x, int y) {
        // TODO: TEMPORARY! Add rules later
        // First create new node to test the position
        GameNode newNode = game.createNextNode();
        GameNode currentNode = game.getCurrentNode();
        int[] gamePosition = newNode.getStoneData();
        int opponentColor = Stone.getOpponentColor(color);

        // Verify the game's expected next color is indeed the one being proposed
        int expectedColor = game.getNextMoveColor();
        if (color != expectedColor)
            return new ProposalResult(ProposalResult.Type.INVALID_COLOR_MISMATCH);

        // Check if another stone is present in this (x, y) location
        if (gamePosition[game.getIndex(x, y)] != Stone.NONE)
            return new ProposalResult(ProposalResult.Type.INVALID_STONE_EXISTS);

        // Check if this is a ko square
        if (gamePosition[game.getIndex(x, y)] == currentNode.getKoPoint())
            return new ProposalResult(ProposalResult.Type.INVALID_KO);

        /*
            Procedures: see https://www.red-bean.com/sgf/ff5/m_vs_ax.htm
         */

        //===========================
        // Step 1: Overwrite
        //===========================
        gamePosition[y * game.getBoardWidth() + x] = color;

        //===========================
        // Step 2: Check for captures
        //===========================
        // First find the neighbors of the current point
        List<Integer> neighbors = game.getNeighborIndices(x, y);

        // Keep track of where we've checked before
        List<Integer> checked = new ArrayList<>();

        // Number of stones captured
        int captures = 0;
        int koPoint = -1;

        // For every neighboring point, seek an enemy string
        for (int point : neighbors) {
            if (gamePosition[point] == opponentColor) {
                StoneString string = seekString(game, point, gamePosition);

                /*
                    If the string has no liberties left,
                    it can be removed from the board.

                    But we will need to then check if the resulting position
                    repeats an earlier position.
                 */
                if (string != null && string.liberties == 0) {
                    // Erase all stones in this string
                    string.stones.forEach(pt -> {
                        gamePosition[pt] = Stone.NONE;
                    });

                    /*
                        If a past state is repeated, the proposal will be rejected.
                        However, the failure type will depend on the number of stones being captured.

                        1 stone = a ko recapture.
                        > 1 stones = a repeating position.

                        The two are really the same thing, just one sounds fancier eh :P
                     */
                    int c = string.stones.size();
                    if (game.isRepeatingPastState(gamePosition)) {
                        // Note: In reality c==1 is never true because invalid kos are already
                        // dismissed at the start of the method to save computation.
                        //
                        // It is kept here for completeness' sake.
                        if (c == 1)
                            return new ProposalResult(ProposalResult.Type.INVALID_KO);
                        else
                            return new ProposalResult(ProposalResult.Type.INVALID_REPEATING);
                    } else {
                        // If there is only 1 capture, then the position of the last captured stone
                        // marks the ko square.
                        if (c == 1)
                            koPoint = string.stones.get(0);
                    }
                    captures += string.stones.size();
                }
            }
        }

        //===========================
        // Step 3: Check for suicide
        //===========================
        // Similar to checking captures, this time we find strings of our own color
        // and check if their liberty == 0.
        int opponentCaptures = 0;
        StoneString selfString = seekString(game, game.getIndex(x, y), gamePosition);
        if (selfString != null && selfString.liberties == 0) {
            if (!isSuicideAllowed())
                return new ProposalResult(ProposalResult.Type.INVALID_SUICIDE);
            else {
                // Erase all stones in this string
                selfString.stones.forEach(pt -> {
                    gamePosition[pt] = Stone.NONE;
                });

                /*
                    If a past state is repeated, the proposal will be rejected.
                    However, the failure type will depend on the number of stones being captured.

                    1 stone = a ko recapture.
                    > 1 stones = a repeating position.

                    The two are really the same thing, just one sounds fancier eh :P
                 */
                if (game.isRepeatingPastState(gamePosition)) {
                    int c = selfString.stones.size();
                    // Here c==1 may be true if both players are trying to suicide 1 stone
                    // consecutively under New Zealand rules.
                    if (c == 1)
                        return new ProposalResult(ProposalResult.Type.INVALID_KO);
                    else {
                        return new ProposalResult(ProposalResult.Type.INVALID_REPEATING);
                    }
                }

                opponentCaptures += selfString.stones.size();
            }
        }

        //===========================
        // Step 4: Final position, update game node data.
        //===========================
        newNode.setStoneData(gamePosition);
        newNode.setCurrentMove(new int[] { x, y });
        newNode.setKoPoint(koPoint);
        newNode.addPrisonersBlack((color == Stone.BLACK) ? opponentCaptures : captures);
        newNode.addPrisonersWhite((color == Stone.BLACK) ? captures : opponentCaptures);

        return new ProposalResult(ProposalResult.Type.SUCCESS, newNode);
    }

    /**
     * Determines whether a suicidal move is permitted.
     *
     * @return True if permitted, false otherwise.
     */
    protected abstract boolean isSuicideAllowed();

    /**
     * Starting at a given point, find all the same color stones connected to it.
     *
     * @param game Current game instance.
     * @param point Current reference point.
     * @param position Current game (board) position.
     * @return A StoneString instance gathered from the reference point.
     */
    private StoneString seekString(Game game, int point, int[] position) {
        // Verify that a stone exists at the given point
        if (position[point] == Stone.NONE)
            return null;

        int color = position[point];
        final int[] liberties = { 0 };
        List<Integer> stoneString = new ArrayList<>();
        List<Integer> visited = new ArrayList<>();
        Stack<Integer> seekStack = new Stack<>();
        seekStack.push(point);

        while (seekStack.size() > 0) {
            int nextPoint = seekStack.pop();
            if (visited.contains(nextPoint))
                continue;

            // If the point has the same color as the current point
            if (position[nextPoint] == color) {
                stoneString.add(nextPoint);
                seekStack.push(nextPoint);
                visited.add(nextPoint);
            }

            List<Integer> neighbors = game.getNeighborIndices(nextPoint);
            neighbors.forEach(pt -> {
                // Seek the neighboring friendly stones
                if (position[pt] == color && !visited.contains(pt)) {
                    seekStack.push(pt);
                }
                // If the point is empty and hasn't been visited before
                // then it counts as a liberty of the string.
                else if (position[pt] == Stone.NONE && !visited.contains(pt)) {
                    liberties[0]++;
                    visited.add(pt);
                }
            });
        }

        StoneString result = new StoneString();
        result.liberties = liberties[0];
        result.stones = stoneString;
        return result;
    }

    @Override
    public boolean submitMove(Game game, ProposalResult proposal) {
        if (!proposal.getType().equals(ProposalResult.Type.SUCCESS))
            return false;

        GameNode newNode = proposal.getNewNode();
        GameNode currentNode = game.getCurrentNode();
        currentNode.addChild(newNode);
        game.setCurrentNode(newNode, true);
        return true;
    }

    class StoneString {

        int liberties;
        List<Integer> stones;

    }
}
