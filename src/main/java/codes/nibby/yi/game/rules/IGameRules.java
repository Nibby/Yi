package codes.nibby.yi.game.rules;

import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameOutcome;

/**
 * Defines the set of rules that govern stone placement, capture and scoring.
 * This is the ancestor class for all GameRules, hence it is very generic.
 *
 * Since Yi plans to support unconventional game rules, all standard rules
 * (e.g. Chinese, Japanese, Korean, Ing, New Zealand etc.) are implemented
 * as StandardGameRules.
 *
 * @author Kevin Yang
 * Created on 27 August 2019
 */
public interface IGameRules {

    /**
     * Checks whether a proposed move at board position (x, y) is allowed.
     *
     * @param color Color of the Go stone.
     * @param x X position of the stone on the Go board.
     * @param y Y position of the stone on the Go board.
     * @return
     */
    ProposalResult proposeMove(Game game, int color, int x, int y);

    /**
     * Formally submits a move to the game object.
     *
     * @param game The game to append the new move.
     * @param proposal Data for the newly proposed move.
     * @return
     */
    boolean submitMove(Game game, ProposalResult proposal);

    /**
     * Analyses a game position and return the outcome according to the rules.
     *
     * @param game Game to be scored.
     * @return A data tuple containing game outcome information.
     */
    GameOutcome scoreGame(Game game);

    /**
     * Amount of komi to be given.
     */
    float getKomi();

    /**
     *
     * @return User-friendly name of the ruleset
     */
    String getName();
}
