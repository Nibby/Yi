package codes.nibby.yi.game;

/**
 * A result block containing information about the final outcome of
 * the game after a scoring algorithm has been performed.
 *
 * TODO: Implement this later! :)
 *
 * @author Kevin Yang
 * Created on 27 August 2019
 */
public class GameOutcome {

    /** Winning stone color, see constants in <strong>Stone</strong> class */
    private int winner;
    private Type resultType;
    private float winAmount;


    /**
     * Different types of game endings.
     */
    public enum Type {
        NO_RESULT,
        ANNULLED,
        RESIGNATION,
        TIMEOUT,
        TIE,
        JIGO
    }
}
